package com.loankuber.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.loankuber.app.LoanKuber.Companion.USER_PREFS

class SharedPrefsUtil(context: Context) {
    val editor: SharedPreferences.Editor = context.getSharedPreferences(USER_PREFS, MODE_PRIVATE).edit()
    val sharedPref: SharedPreferences = context.getSharedPreferences(USER_PREFS, MODE_PRIVATE)


    companion object{
        const val AGENT_NAME = "agentName"
        var instance: SharedPrefsUtil? = null
        fun getInstance(context: Context): SharedPrefsUtil?{
            if(instance == null){
                instance = SharedPrefsUtil(context)
            }
            return instance
        }
    }

    fun put(key: String, value: String){
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String?{
        return sharedPref.getString(key, null)
    }
}