package com.loankuber.app.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.app.utils.SharedPrefsUtil
import com.loankuber.library.utils.KotlinUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mRoot: DatabaseReference

    private lateinit var loginProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        mRoot = Firebase.database.reference

        loginProgressDialog = ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }

        val emailField: EditText = findViewById(R.id.email)
        val passwordField: EditText = findViewById(R.id.password)
        val loginBtn: Button = findViewById(R.id.login_btn)

        loginBtn.setOnClickListener {
            loginProgressDialog.show()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                authResult.user?.let {
                    gotoDetailsActivity(it.uid)
                } ?: run {
                    loginProgressDialog.dismiss()
                    toast("Login Failed")
                }
            }
        }

    }

    private fun gotoDetailsActivity(uid: String){
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot: DataSnapshot
            try {
                snapshot = mRoot.child("Agents").child(uid).get().await()
            }
            catch (e: Exception){
                loginProgressDialog.dismiss()
                toast("Login Failed")
                return@launch
            }

            if(!snapshot.exists()){
                loginProgressDialog.dismiss()
                toast("Agent data not found, please contact admin")
                auth.signOut()
                return@launch
            }

            SharedPrefsUtil.getInstance(this@LoginActivity)?.put(SharedPrefsUtil.AGENT_NAME, snapshot.child("name").value.toString())
            val detailsIntent = Intent(this@LoginActivity, DetailActivity::class.java)
            loginProgressDialog.dismiss()
            startActivity(detailsIntent)
            finish()
        }
    }
}