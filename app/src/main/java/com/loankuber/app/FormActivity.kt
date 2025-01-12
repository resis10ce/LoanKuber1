package com.loankuber.app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.loankuber.app.databinding.ActivityFormBinding
import java.io.InputStream
import java.util.Collections


class FormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBinding
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_form)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    binding.image.setImageBitmap(it)
                }
            }
        }

        binding.addPhoto.setOnClickListener {
            checkCameraPermission()
        }

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
            // Permission already granted
            onCameraPermissionGranted()
        } else {
            // Request the permission
            requestCameraPermission.launch(cameraPermission)
        }
    }

    private fun onCameraPermissionGranted() {
        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        openCamera()
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        // Inform the user or handle the lack of permission
    }


    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(cameraIntent)
        }
    }
//    private inner class AppendDataTask : AsyncTask<List<List<Any>>, Void, String>() {
//        override fun doInBackground(vararg params: List<List<Any>>): String? {
//            try {
//                val inputStream: InputStream = assets.open(JSON_FILE)
//                val credential = GoogleCredential.fromStream(inputStream)
//                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS))
//
//                val transport = AndroidHttp.newCompatibleTransport()
//                val jsonFactory = GsonFactory.getDefaultInstance()
//
//                val service = Sheets.Builder(transport, jsonFactory, credential)
//                    .setApplicationName(APPLICATION_NAME)
//                    .build()
//
//                // Key change for appending: Use "APPEND" and specify the sheet name
//                val range = "$SHEET_NAME!A:B" // Append to columns A and B
//                val body = ValueRange().setValues(params[0])
//
//                val appendRequest = service.spreadsheets().values().append(SPREADSHEET_ID, range, body)
//                appendRequest.valueInputOption = "USER_ENTERED" // Or "RAW"
//                val result: AppendValuesResponse = appendRequest.execute()
//
//                return "Data appended successfully. Updated range: ${result.updates.updatedRange}"
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return "Error appending data: ${e.message}"
//            }
//        }
//
//        override fun onPostExecute(result: String?) {
//            result?.let {
//                Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
//            }
//        }
  //  }

}