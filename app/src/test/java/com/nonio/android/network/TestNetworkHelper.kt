package com.nonio.android.network

import android.content.Context
import com.nonio.android.common.UserProvider
import com.nonio.android.model.UserModel
import com.nonio.android.network.interceptor.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object TestNetworkHelper {
    val apiService: ApiService

    init {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(Urls.BASE_URL)
                // .addCallAdapterFactory(ResponseCallAdapterFactory())
//            .addConverterFactory(ApiResultConverterFactory)
//            .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(
                    MoshiConverterFactory.create(),
                ).addConverterFactory(ScalarsConverterFactory.create())
                .client(
                    OkHttpClient
                        .Builder()
                        .addInterceptor(
                            TokenInterceptor(
                                object : UserProvider {
                                    override fun init(context: Context) {
                                    }

                                    override fun getToken(): String = ""

                                    override fun isLogin(): Boolean = false

                                    override fun getRefreshToken(): String = ""

                                    override fun updateToken(
                                        refreshToken: String,
                                        accessToken: String,
                                    ) {
                                    }
                                },
                                onRefreshToken = {
                                    return@TokenInterceptor UserModel(
                                        accessToken = "",
                                        refreshToken = "",
                                        null,
                                        null,
                                    )
                                },
                            ),
                        ).addInterceptor(
                            HttpLoggingInterceptor().apply {
                                setLevel(HttpLoggingInterceptor.Level.BODY)
                            },
                        ).build(),
                ).build()

        apiService = retrofit.create(ApiService::class.java)
    }
}

// map to json to requset body
fun Map<String, *>.toRequestBody(): RequestBody {
    val json = JSONObject(this).toString()
    return json.toRequestBody()
}
