package com.loankuber.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class LoanKuber: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("TEST", "onCreate: ")
        FirebaseApp.initializeApp(this)
    }
}