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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.loankuber.admin.databinding.ActivityCreateAgentBinding
import com.loankuber.library.utils.KotlinUtils.toast

class CreateAgentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAgentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mRoot: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_agent)

        auth = Firebase.auth
        mRoot = Firebase.database.reference

        binding.createAgent.setOnClickListener {
            if(binding.name.text.isEmpty() || binding.email.text.isEmpty() || binding.password.text.isEmpty()){
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){

                        toast("currentUserId: ${auth.currentUser!!.uid}")

                        mRoot.child("Agents").child(auth.currentUser!!.uid).setValue(
                            mapOf(
                                "name" to binding.name.text.toString()
                            )
                        ).addOnSuccessListener {
                            toast("Agent created")
                            auth.signOut()
                            val loginIntent = Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(loginIntent)
                            finish()
                        }.addOnFailureListener {
                            toast("Failed to create agent")
                        }

                    } else {
                        Toast.makeText(this, "Failed to create agent", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}