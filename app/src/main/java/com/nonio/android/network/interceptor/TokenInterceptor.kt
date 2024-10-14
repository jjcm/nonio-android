package com.nonio.android.network.interceptor

import com.nonio.android.common.UserProvider
import com.nonio.android.model.UserModel
import com.nonio.android.network.Urls
import com.nonio.android.network.toRequestBody
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import timber.log.Timber

class TokenInterceptor(
    private val userProvider: UserProvider,
    val onRefreshToken: suspend (RequestBody) -> UserModel?,
) : Interceptor {
    @Synchronized
    private fun refreshToken(): Boolean {
        var refreshToken: String? = userProvider.getRefreshToken()
        if (refreshToken.isNullOrBlank()) return false

        val model =
            runBlocking {
                runCatching {
                    val model = onRefreshToken.invoke(mapOf("refreshToken" to refreshToken).toRequestBody())
                    model
                }.onFailure {
                    it.printStackTrace()
                }.getOrNull()
            }

        val accessToken = model?.accessToken
        refreshToken = model?.refreshToken
        return if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            userProvider.updateToken(refreshToken, accessToken)
            true
        } else {
            false
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (userProvider.isLogin() &&
            (
                url.startsWith(Urls.BASE_URL) ||
                    url.startsWith(Urls.UPLOAD_IMAGE) ||
                    url.startsWith(Urls.UPLOAD_VIDEO) ||
                    url.startsWith(Urls.MOVE_IMAGE) ||
                    url.startsWith(Urls.MOVE_VIDEO)

            )
        ) {
            val token = userProvider.getToken()
            Timber.d("Current token $token")
            var newRequest =
                request
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

            val response = chain.proceed(newRequest)
            if (response.code == 401) {
                Timber.d("Token has expired! Going to refresh")
                response.close()
                val tokenRefreshed = refreshToken()
                if (tokenRefreshed) {
                    val newAccessToken = userProvider.getToken()
                    Timber.d("Token refresh successful! New token $newAccessToken")
                    newRequest =
                        newRequest
                            .newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newAccessToken")
                            .build()
                    return chain.proceed(newRequest)
                } else {
                    Timber.d("Token refresh failed!")
                }
            }
            return response
        }
        return chain.proceed(request)
    }
}
