package com.loankuber.app.ui.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.library.utils.KotlinUtils.toast


class SummaryFragment : Fragment(R.layout.fragment_summary) {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var progressDialog: ProgressDialog

    private lateinit var parentActivity: DetailActivity

    private lateinit var webView: WebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentActivity = requireActivity() as DetailActivity

        // Initialize all the location related variables along with location callback where we would get the location result
        initializeLocationData()

        val loanNumber = view.findViewById<TextView>(R.id.loan_number)
        val name = view.findViewById<TextView>(R.id.customer_name)
        val nextVisit = view.findViewById<TextView>(R.id.next_visit_date)
        val outcome = view.findViewById<TextView>(R.id.outcome)
        val customerImage = view.findViewById<ImageView>(R.id.customer_image)

        webView = view.findViewById<WebView>(R.id.webView);

        loanNumber.text = "(${parentActivity.loanNumber})"
        name.text = parentActivity.name
        nextVisit.text = parentActivity.nextVisitDate
        if(parentActivity.outcome != "PTP")
            outcome.text = parentActivity.outcome
        else
            outcome.text = "PTP on ${parentActivity.ptpDate}"

        if(parentActivity.savedBitmap != null){
            customerImage.setImageBitmap(parentActivity.savedBitmap)
        }

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Getting location... Pleas wait...")
            setCancelable(false)
        }

        // This function will get the current location of the user (after checking the location permission)
        getCurrentLocation()

    }

    private fun initializeLocationData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(500)
            setMaxUpdateDelayMillis(1000)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val mapsLink =
                        "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    fusedLocationClient.removeLocationUpdates(this)
                    Toast.makeText(requireContext(), mapsLink, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()

                    // Enable JavaScript and other settings
                    val webSettings = webView.settings
                    webSettings.setJavaScriptEnabled(true)
                    webSettings.domStorageEnabled = true

                    webView.webViewClient = WebViewClient()
                    webView.loadUrl("file:///android_asset/map.html")


                    // Wait until WebView loads, then pass lat/lng
//                    webView.postDelayed({
//                        val jsCommand = "updateMap(" + "23.3441" + ", " + "85.3096" + ")"
////                        val jsCommand = (("updateMap(${location.latitude}").toString() + ", " + location.longitude) + ")"
//                        webView.evaluateJavascript(jsCommand, null)
//                    }, 1000)

                    return
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        progressDialog.show()
        if (!parentActivity.getLocationPermissionHandler().isPermissionGranted(requireContext())) {
            toast("Location permission is required to get your location.")
            progressDialog.dismiss()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}