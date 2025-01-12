//package com.loankuber.app.utils
//
//import android.content.Context
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
//import com.google.api.client.http.HttpTransport
//import java.io.InputStream
//
//
//class GoogleSheet(context: Context) {
//    init {
//        val inputStream: InputStream = context.getAssets().open("credentials.json")
//        val credential: GoogleCredential = GoogleCredential.fromStream(inputStream)
//            .createScoped(setOf("https://www.googleapis.com/auth/spreadsheets"))
//
////        val service: Sheets = Builder(
////            HttpTransport.newTrustedTransport(),
////            JacksonFactory.getDefaultInstance(),
////            credential
////        )
////            .setApplicationName("Your App Name")
////            .build()
//    }
//
//}