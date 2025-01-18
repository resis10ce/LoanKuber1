package com.loankuber.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.app.utils.SharedPrefsUtil


class LauncherActivity : AppCompatActivity() {

    private val mRoot = Firebase.database.reference
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        if(auth.currentUser != null){
            gotoDetailsActivity(auth.currentUser!!.uid)
        }
        else{
            Handler().postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 800)
        }

    }

    private fun gotoDetailsActivity(uid: String){
        mRoot.child("Agents").child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                SharedPrefsUtil.getInstance(this)?.put(SharedPrefsUtil.AGENT_NAME, it.child("name").value.toString())
                val mainIntent = Intent(this, DetailActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
            else{
                Toast.makeText(
                    this,
                    "Agent data not found, please contact admin",
                    Toast.LENGTH_SHORT
                ).show()
                auth.signOut()
            }
        }
    }
}