package com.loankuber.app.models

import androidx.annotation.Keep

@Keep
data class CustomerData(
    val agentName:String,
    val customerName: String,
    val loanNumber: String,
    val image: String,
    val map: String,
    val nextVisit: String,
    val outcome: String,
    val ptpDate: String?
)
