package com.loankuber.app

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.loankuber.app.models.CustomerData
import com.loankuber.app.ui.fragments.FillDetailsFragment
import com.loankuber.app.ui.fragments.GetCustomerFragment
import com.loankuber.app.ui.fragments.PhotoFragment
import com.loankuber.app.ui.fragments.SummaryFragment
import com.loankuber.app.utils.RetrofitInstance
import com.loankuber.app.utils.SharedPrefsUtil
import com.loankuber.library.permissions.LocationPermissionHandler
import com.loankuber.library.utils.KotlinUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailActivity : AppCompatActivity() {

    private val locationPermissionHandler = LocationPermissionHandler(this, this)

    private val fragments = listOf(GetCustomerFragment(), FillDetailsFragment(), PhotoFragment(), SummaryFragment())
    private var currentFragmentIndex = 0

    // These 2 variables store the user image payload and the bitmap of the image
    var userImage: String? = null
    var savedBitmap: Bitmap? = null

    var name: String? = null
    var loanNumber: String? = null
    var ptpDate: String? = null
    var nextVisitDate: String? = null
    var outcome: String? = null
    var maps: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val previousBtn = findViewById<ImageButton>(R.id.btndetail_prev_btn)


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

    fun showNextFragment() {
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
            onBackPressed()
        }
    }

    private fun checkAndRequestLocationPermission() {
        locationPermissionHandler.requestPermission { isGranted ->
            if (!isGranted) toast("Location permission denied")
        }
    }

    fun setCustomerDetails(name: String?, loanNumber: String?) {
        this.name = name
        this.loanNumber = loanNumber
    }

    fun hasCustomerDetails() = name != null && loanNumber != null

    fun hasFilledDetails(): Boolean {
        if(outcome == null || nextVisitDate == null) {
            return false
        }
        else if(outcome.equals("PTP") && ptpDate == null) {
            return false
        }
        return true
    }

    fun getLocationPermissionHandler() = locationPermissionHandler



    fun postLoanDetails() {
        var progressDialog = ProgressDialog(this).apply {
            setMessage("Submitting, Pleas wait... ")
            setCancelable(false)
        }
        progressDialog.show()
        val agentName = SharedPrefsUtil.getInstance(this@DetailActivity)?.getString(SharedPrefsUtil.AGENT_NAME)
        if(agentName == null){
            Toast.makeText(this, "Agent Name not found, please contact admin", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        val customerData = CustomerData(agentName, name!!, loanNumber !!, userImage!! , maps!!, nextVisitDate!!, outcome!! ,ptpDate!!)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitInstance.api.postLoanDetails("insert", customerData)
                withContext(Dispatchers.Main) {
                    // Resetting values after successful submission
                    resetValues()
                    progressDialog.dismiss()
                    Toast.makeText(this@DetailActivity, "Submitted", Toast.LENGTH_SHORT).show()
                    showFragment(0)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Log.e("TESTER", "postLoanDetails: $e")
                    Toast.makeText(this@DetailActivity, "Error: ${e.message} ${e}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetValues() {
        name = null
        loanNumber = null
        userImage = null
        ptpDate = null
        nextVisitDate = null
        outcome = null
        ptpDate=null
    }
}