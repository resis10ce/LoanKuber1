package com.loankuber.app

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.initialize

class LoanKuber: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("TEST", "onCreate: ")
        FirebaseApp.initializeApp(this)
    }
}