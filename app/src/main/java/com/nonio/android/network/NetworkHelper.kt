package com.nonio.android.network

import com.nonio.android.common.UserHelper
import com.nonio.android.network.interceptor.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object NetworkHelper {
    val apiService: ApiService

    init {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(Urls.BASE_URL)
                // .addCallAdapterFactory(ResponseCallAdapterFactory())
//            .addConverterFactory(ApiResultConverterFactory)
//            .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(
                    MoshiConverterFactory.create(),
                ).client(
                    OkHttpClient
                        .Builder()
                        .addInterceptor(
                            TokenInterceptor(
                                UserHelper,
                                onRefreshToken = ::refreshToken,
                            ),
                        ).addInterceptor(
                            HttpLoggingInterceptor().apply {
                                setLevel(HttpLoggingInterceptor.Level.BODY)
                            },
                        ).build(),
                ).build()

        apiService = retrofit.create(ApiService::class.java)
    }

    private suspend fun refreshToken(requestBody: RequestBody) = apiService.refreshToken(requestBody)
}

// map to json to requset body
fun Map<String, *>.toRequestBody(): RequestBody {
    val json = JSONObject(this).toString()
    return json.toRequestBody()
}
