package com.loankuber.app.services
import com.loankuber.app.models.CustomerData
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("exec")
    suspend fun postLoanDetails(
        @Query("uName") uName: String,
        @Query("loanNumber") loanNumber: String,
        @Body uImage: CustomerData,
        @Query("action") action: String
    )
}