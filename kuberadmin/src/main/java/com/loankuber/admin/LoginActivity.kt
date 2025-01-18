package com.loankuber.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.loankuber.admin.databinding.ActivityLoginBinding
import com.loankuber.library.utils.KotlinUtils.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.loginBtn.setOnClickListener {
            if(binding.email.text.isEmpty() || binding.password.text.isEmpty()){
                toast("Please fill all the details")
                return@setOnClickListener
            }

            // FIXME: This is a hardcoded email. This should be fixed
            // TODO: Address the above FIXME
            if(binding.email.text.toString() != "admin@gmail.com"){
                toast("Error")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val homeIntent = Intent(this, HomeActivity::class.java)
                        startActivity(homeIntent)
                        finish()
                    } else {
                        toast("Failed to login")
                    }
                }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val homeIntent = Intent(this, HomeActivity::class.java)
            startActivity(homeIntent)
        }
    }
}