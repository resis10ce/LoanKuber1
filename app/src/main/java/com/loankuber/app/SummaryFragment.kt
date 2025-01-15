package com.loankuber.app

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class SummaryFragment : Fragment(R.layout.fragment_summary) {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var progressDialog: ProgressDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Getting location... Pleas wait...")
            setCancelable(false)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getCurrentLocation()

    }

    private fun getCurrentLocation() {
        progressDialog.show()
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(500)
            setMaxUpdateDelayMillis(1000)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val mapsLink = "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    fusedLocationClient.removeLocationUpdates(this)
                    Toast.makeText(requireContext(), mapsLink, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    return
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}