package com.loankuber.app

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailActivity : AppCompatActivity() {

    val fragments = listOf(DetailsFragment(), PhotoFragment(), SummaryFragment())
    var currentFragmentIndex = 0

    var userImage: String? = null
    var savedBitmap: Bitmap? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }

            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }

            else -> {
                // No location access granted.
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        checkPermissionsAndGetLocation()
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

    private fun checkPermissionsAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Toast.makeText(
                    this,
                    "Location permission is needed to show your location on map.",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

}