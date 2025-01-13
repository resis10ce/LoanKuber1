package com.loankuber.admin

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class KuberAdmin: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}