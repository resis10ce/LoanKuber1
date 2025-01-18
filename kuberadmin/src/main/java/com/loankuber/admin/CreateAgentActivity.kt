package com.loankuber.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.loankuber.admin.databinding.ActivityCreateAgentBinding

class CreateAgentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAgentBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_agent)

        auth = Firebase.auth

        binding.createAgent.setOnClickListener {
            if(binding.name.text.isEmpty() || binding.email.text.isEmpty() || binding.password.text.isEmpty()){
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "Agent created", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                        val loginIntent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(loginIntent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to create agent", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}