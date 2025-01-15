package com.loankuber.library.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.loankuber.library.utils.KotlinUtils.toast

class LocationPermissionHandler(private val activity: Activity, lifecycleOwner: LifecycleOwner) :
    PermissionHandler(lifecycleOwner) {

    private val locationPermissionFine = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val locationPermissionCoarse = android.Manifest.permission.ACCESS_COARSE_LOCATION

    override fun isPermissionGranted(context: Context): Boolean {
        return isSinglePermissionGranted(locationPermissionFine) || isSinglePermissionGranted(
            locationPermissionCoarse
        )
    }

    private fun isSinglePermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(callback: ((Boolean) -> Unit)?) {
        this.callback = callback

        when {
            isSinglePermissionGranted(locationPermissionFine) || isSinglePermissionGranted(
                locationPermissionCoarse
            ) -> {
                callback?.invoke(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                locationPermissionFine
            ) -> {
                activity.toast("Location permission is needed to show your location on map.")
                requestMultiplePermissionsLauncher?.launch(
                    arrayOf(
                        locationPermissionFine,
                        locationPermissionCoarse
                    )
                )
            }

            else -> {
                requestMultiplePermissionsLauncher?.launch(
                    arrayOf(
                        locationPermissionFine,
                        locationPermissionCoarse
                    )
                )
            }
        }
    }
}