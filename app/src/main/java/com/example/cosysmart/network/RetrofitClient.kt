
package com.example.cosysmart.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Client para las peticiones a la API

object RetrofitClient {

    private const val BASE_URL = "http://localhost:8000/"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Construye la URL de las fotografías del perfil.
   fun construirUrl(ruta: String): String {
        return if (
            ruta.startsWith("http://") ||
            ruta.startsWith("https://")
        ) {
            ruta
        } else {
            BASE_URL.trimEnd('/') + "/" + ruta.trimStart('/')
        }
    }

    val instance: ApiService by lazy {

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->

                val originalRequest = chain.request()

                val token = appContext?.let { context ->

                    val preferences = context.getSharedPreferences(
                        "CosyPrefs",
                        Context.MODE_PRIVATE
                    )

                    preferences.getString("JWT_TOKEN", null)
                }

                val newRequest = if (!token.isNullOrBlank()) {

                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()

                } else {
                    originalRequest
                }

                chain.proceed(newRequest)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
