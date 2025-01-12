package com.loankuber.app

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.loankuber.app.utils.SharedPrefsUtil

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mRoot: DatabaseReference

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        mRoot = Firebase.database.reference

        progressDialog = ProgressDialog(this).apply {
            setMessage("Checking agent data, please wait...")
            setCancelable(false)
        }

        val emailField: EditText = findViewById(R.id.email)
        val passwordField: EditText = findViewById(R.id.password)
        val loginBtn: Button = findViewById(R.id.login_btn)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        gotoFormActivity()
                    } else {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            progressDialog.show()
            mRoot.child("Agents").child(currentUser.uid).get().addOnSuccessListener {
                if (it.exists()) {
                    SharedPrefsUtil.getInstance(this)?.put(SharedPrefsUtil.AGENT_NAME, it.child("name").value.toString())
                    gotoFormActivity()
                    progressDialog.dismiss()
                }
                else{
                    Toast.makeText(
                        this,
                        "Agent data not found, please contact admin",
                        Toast.LENGTH_SHORT
                    ).show()
                    auth.signOut()
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun gotoFormActivity(){
        val mainIntent = Intent(this, FormActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}