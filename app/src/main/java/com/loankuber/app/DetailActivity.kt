package com.loankuber.app

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailActivity : AppCompatActivity() {

    val fragments = listOf(DetailsFragment(), PhotoFragment(), SummaryFragment())
    var currentFragmentIndex = 0

    var userImage: String? = null
    var savedBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prev_btn = findViewById<Button>(R.id.btndetail_prev_btn)
        val next_btn = findViewById<Button>(R.id.btndetail_next_btn)


        /*
        showFragment is used for fragemnt tranction than rest all functions like  showNextFragment, showPreviousFragment is just maintaing the currentFragmentIndex
        variable and calling the showFragment function.
         */
        showFragment(currentFragmentIndex)
        prev_btn.setOnClickListener {
            showPreviousFragment()
        }
        next_btn.setOnClickListener {
            showNextFragment()
        }
    }


         fun showFragment(index: Int) {
            val fragment = fragments[index]
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.detail_fragment, fragment)
                commit()
            }
        }

         fun showNextFragment() {
            if (currentFragmentIndex < fragments.size - 1) {
                currentFragmentIndex++
                showFragment(currentFragmentIndex)
            } else {
                Toast.makeText(this, "No more fragments", Toast.LENGTH_SHORT).show()
            }
        }

         fun showPreviousFragment() {
            if (currentFragmentIndex > 0) {
                currentFragmentIndex--
                showFragment(currentFragmentIndex)
            } else {
                Toast.makeText(this, "No previous fragments", Toast.LENGTH_SHORT).show()
            }
        }


}