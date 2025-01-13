package com.loankuber.app.ui

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import com.google.android.gms.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.loankuber.app.R
import com.loankuber.app.utils.RetrofitInstance
import com.loankuber.app.databinding.ActivityFormBinding
import com.loankuber.app.models.CustomerData
import com.loankuber.app.utils.SharedPrefsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var rbitmap: Bitmap? = null
    private var userImage: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_form)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Submitting, Pleas wait...")
            setCancelable(false)
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val resizedBitmap = getResizedBitmap(bitmap, 250)
                    rbitmap = resizedBitmap
                    userImage = getStringImage(resizedBitmap)
                    binding.image.setImageBitmap(it)
                }
            }
        }

        checkPermissionsAndGetLocation()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.addPhoto.setOnClickListener {
            checkCameraPermission()
        }

        binding.submitBtm.setOnClickListener {
            if (userImage == null) {
                Toast.makeText(this, "No Image Found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            getCurrentLocation()
        }
    }

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

    private fun checkPermissionsAndGetLocation() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Location permission is needed to show your location on map.", Toast.LENGTH_SHORT).show()
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
                    val name = binding.name.text.toString()
                    val loanNumber = binding.laonNumber.text.toString()

                    postLoanDetails(name, loanNumber, mapsLink)

                    fusedLocationClient.removeLocationUpdates(this)
                    return
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun checkCameraPermission() {
        val cameraPermission = android.Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted()
        } else {
            requestCameraPermission.launch(cameraPermission)
        }
    }

    private fun onCameraPermissionGranted() {
        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        openCamera()
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(cameraIntent)
        }
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun getStringImage(bmp: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun postLoanDetails(customerName: String, loanNumber: String, map: String) {

        val agentName = SharedPrefsUtil.getInstance(this@FormActivity)?.getString(SharedPrefsUtil.AGENT_NAME)
        if(agentName == null){
            Toast.makeText(this, "Agent Name not found, please contact admin", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        val customerData = CustomerData(agentName, customerName, loanNumber, userImage!!, map)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitInstance.api.postLoanDetails("insert", customerData)
                withContext(Dispatchers.Main) {
                    binding.name.setText("")
                    binding.laonNumber.setText("")
                    binding.image.setImageDrawable(getDrawable(R.drawable.no_image))
                    userImage = null
                    rbitmap = null
                    progressDialog.dismiss()
                    Toast.makeText(this@FormActivity, "Submitted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Log.e("TESTER", "postLoanDetails: $e")
                    Toast.makeText(this@FormActivity, "Error: ${e.message} ${e}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}