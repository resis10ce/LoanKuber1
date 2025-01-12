package com.loankuber.app.services
import com.loankuber.app.models.CustomerData
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("exec")
    suspend fun postLoanDetails(
        @Query("action") action: String,
        @Body uImage: CustomerData
    )
}