package com.loankuber.app

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.loankuber.library.permissions.LocationPermissionHandler
import com.loankuber.library.utils.KotlinUtils.toast

class DetailActivity : AppCompatActivity() {

    private val locationPermissionHandler = LocationPermissionHandler(this, this)

    private val fragments = listOf(DetailsFragment(), PhotoFragment(), SummaryFragment())
    private var currentFragmentIndex = 0

    // These 2 variables store the user image payload and the bitmap of the image
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

        val previousBtn = findViewById<Button>(R.id.btndetail_prev_btn)
        val nextBtn = findViewById<Button>(R.id.btndetail_next_btn)


        /*
        showFragment is used for fragment transaction
        Rest of the functions like showNextFragment, showPreviousFragment is just maintaining the currentFragmentIndex
        variable and calling the showFragment function.
         */
        // Show the first fragment
        showFragment(currentFragmentIndex)

        previousBtn.setOnClickListener {
            showPreviousFragment()
        }

        nextBtn.setOnClickListener {
            showNextFragment()
        }

        // Checking the location permission once the activity opens and requesting the permission if not already granted
        checkAndRequestLocationPermission()
    }


    private fun showFragment(index: Int) {
        val fragment = fragments[index]
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.detail_fragment, fragment)
            commit()
        }
    }

    private fun showNextFragment() {
        if (currentFragmentIndex < fragments.size - 1) {
            currentFragmentIndex++
            showFragment(currentFragmentIndex)
        } else {
            toast("No more fragments")
        }
    }

    private fun showPreviousFragment() {
        if (currentFragmentIndex > 0) {
            currentFragmentIndex--
            showFragment(currentFragmentIndex)
        } else {
            toast("No previous fragments")
        }
    }

    private fun checkAndRequestLocationPermission() {
        locationPermissionHandler.requestPermission { isGranted ->
            if (!isGranted) toast("Location permission denied")
        }
    }

    fun getLocationPermissionHandler() = locationPermissionHandler

}