package com.loankuber.library.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraPermissionHandler(lifecycleOwner: LifecycleOwner) : PermissionHandler(lifecycleOwner) {

    private val cameraPermission = Manifest.permission.CAMERA

    override fun isPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(callback: ((Boolean) -> Unit)?) {
        this.callback = callback
        requestMultiplePermissionsLauncher?.launch(arrayOf(cameraPermission))
    }

}