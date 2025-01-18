package com.loankuber.app.ui.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
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
import com.google.firebase.firestore.GeoPoint
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.app.models.CustomerData
import com.loankuber.app.utils.RetrofitInstance
import com.loankuber.app.utils.SharedPrefsUtil
import com.loankuber.library.utils.KotlinUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
//import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SummaryFragment : Fragment(R.layout.fragment_summary) {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var progressDialog: ProgressDialog
    private lateinit var progressDialogsubmit:ProgressDialog

    private lateinit var parentActivity: DetailActivity

    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Configure osmdroid (needed for storage and cache)
        Configuration.getInstance().load(requireContext().applicationContext,
            requireContext().getSharedPreferences("osmdroid", MODE_PRIVATE));
        super.onViewCreated(view, savedInstanceState)

        parentActivity = requireActivity() as DetailActivity

        // Initialize all the location related variables along with location callback where we would get the location result
        initializeLocationData()

        val loanNumber = view.findViewById<TextView>(R.id.loan_number)
        val name = view.findViewById<TextView>(R.id.customer_name)
        val nextVisit = view.findViewById<TextView>(R.id.next_visit_date)
        val today = view.findViewById<TextView>(R.id.today)
        val outcome = view.findViewById<TextView>(R.id.outcome)
        val customerImage = view.findViewById<ImageView>(R.id.customer_image)
        val submit_btn = view.findViewById<Button>(R.id.submit_btm)

        mapView = view.findViewById(R.id.mapView);

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

        val date = Date()
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val stringDate = format.format(date)
        today.text = stringDate

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Getting location... Pleas wait...")
            setCancelable(false)
        }
        progressDialogsubmit = ProgressDialog(requireContext()).apply {
            setMessage("Getting location... Pleas wait...")
            setCancelable(false)
        }

        // This function will get the current location of the user (after checking the location permission)
//        getCurrentLocation()  // Moved to onResume()

        submit_btn.setOnClickListener {

            parentActivity.postLoanDetails()
            //progressDialog.dismiss()
        }
//End of this fragment
    }


    private fun addMarker(point: org.osmdroid.util.GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.title = title
        mapView.overlays.add(marker)
        mapView.invalidate()
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
                    parentActivity.maps=mapsLink
                    progressDialog.dismiss()


                    // Set up mapView
                    mapView.setTileSource(TileSourceFactory.MAPNIK)
                    mapView.setMultiTouchControls(true)

                    val startPoint =
                        org.osmdroid.util.GeoPoint(location.latitude, location.longitude)
                    mapView.controller.setZoom(18)
                    mapView.controller.setCenter(startPoint)

                    addMarker(startPoint, "Customer Location")


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

    override fun onResume() {
        super.onResume()
        getCurrentLocation() // Call getCurrentLocation() here to start location updates
    }



}