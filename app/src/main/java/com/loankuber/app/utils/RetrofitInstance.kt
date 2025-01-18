package com.loankuber.app.utils
import com.loankuber.app.services.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycby5f-SzpeTHKyB5OPLwVrtSXw2cdK0ykvkwKZ6o0zdIJUJuA2YhY3Q9ungIFI4Zzig/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}