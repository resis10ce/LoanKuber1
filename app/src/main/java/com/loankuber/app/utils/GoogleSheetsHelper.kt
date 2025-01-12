//package com.loankuber.app.utils
//
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
//import com.google.api.client.json.JsonFactory
//import com.google.api.client.json.jackson2.JacksonFactory
//import com.google.api.client.util.IOUtils
//import com.google.auth.oauth2.GoogleCredentials
//import com.google.api.services.sheets.v4.Sheets
//import com.google.api.services.sheets.v4.model.ValueRange
//import java.io.FileInputStream
//import java.time.temporal.ValueRange
//
//object GoogleSheetsHelper {
//
//    private const val APPLICATION_NAME = "Your App Name"
//    private const val SPREADSHEET_ID = "your-google-sheet-id" // Replace with your Google Sheet ID
//    private const val CREDENTIALS_FILE_PATH = "path-to-your-service-account.json" // Replace with JSON key file path
//
//    private val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
//    private val transport = GoogleNetHttpTransport.newTrustedTransport()
//
//    private fun getSheetsService(): Sheets {
//        val credentials = GoogleCredentials.fromStream(FileInputStream(CREDENTIALS_FILE_PATH))
//            .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))
//
//        return Sheets.Builder(transport, jsonFactory) {
//            credentials.initialize(it)
//        }.setApplicationName(APPLICATION_NAME).build()
//    }
//
//    fun addRowToSheet(range: String, values: List<Any>) {
//        val sheetsService = getSheetsService()
//        val body = ValueRange().setValues(listOf(values))
//        sheetsService.spreadsheets().values().append(SPREADSHEET_ID, range, body)
//            .setValueInputOption("RAW")
//            .execute()
//    }
//}
