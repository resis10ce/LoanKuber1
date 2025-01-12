package com.loankuber.app
import com.loankuber.app.services.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyFuBm5rmvANaKXO_FzsKKI6UXmqAcK0lD5CKSwq7JnzuwoO2JeVlaF8hgGlztcvT8f/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}