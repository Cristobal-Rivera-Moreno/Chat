package com.cristobal.chat.api_services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitInstance {
    private const val BASE_URL = "http://10.30.0.72:8000/api/" // Example Base URL

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor("app_chat_secret_!#@%"))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <Api> buildApi(api: Class<Api>): Api {
        return retrofit.create(api)
    }
}

private class AuthInterceptor(private val authToken: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $authToken") // Add your header here
            .addHeader("Content-Type", "application/json") // Add other headers if needed
            .build()
        return chain.proceed(newRequest)
    }
}