package com.loankuber.library.permissions

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

abstract class PermissionHandler(lifecycleOwner: LifecycleOwner) : DefaultLifecycleObserver {

    protected var callback: ((Boolean) -> Unit)? = null
    protected var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        requestMultiplePermissionsLauncher = when (lifecycleOwner) {
            is ComponentActivity -> lifecycleOwner.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.values.all { it }) {
                    callback?.invoke(true)
                } else {
                    callback?.invoke(false)
                }
            }

            is Fragment -> lifecycleOwner.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.values.all { it }) {
                    callback?.invoke(true)
                } else {
                    callback?.invoke(false)
                }
            }

            else -> throw IllegalArgumentException("Lifecycle owner must be Activity or Fragment")
        }
    }

    abstract fun isPermissionGranted(context: Context): Boolean

    abstract fun requestPermission(callback: ((Boolean) -> Unit)?)

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
        requestMultiplePermissionsLauncher = null
    }

}