package com.example.cheap.services
import ClientService
import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://cheap-app.ddns.net/api/"
    private const val AUTH_HEADER = "Authorization"
    private const val TOKEN_PREFIX = "Bearer "

    // Create an OkHttpClient with an Interceptor to add the Bearer Token
    private fun createOkHttpClient(token: String?): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        if (!token.isNullOrEmpty()) {
            val interceptor = Interceptor { chain ->
                val originalRequest: Request = chain.request()

                // Add Bearer Token to the request headers
                val newRequest: Request = originalRequest.newBuilder()
                    .header(AUTH_HEADER, TOKEN_PREFIX + token)
                    .build()

                chain.proceed(newRequest)
            }
            okHttpClientBuilder.addInterceptor(interceptor)
        }
        return okHttpClientBuilder.build()
    }

    fun getClientService(token: String? = null): ClientService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ClientService::class.java)
    }
}
