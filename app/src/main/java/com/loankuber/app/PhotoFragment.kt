package com.loankuber.app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.loankuber.app.utils.ImageUtils

/*

* once the add clicks the camera button than it checks the camera permission and open the camera if granted
* cameraLauncher receive the result once the user click  the photo

*/
class PhotoFragment : Fragment(R.layout.fragment_photo) {
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onCameraPermissionGranted()
            } else {
                onCameraPermissionDenied()
            }
        }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val parentActivity = requireActivity() as DetailActivity

        val saveImage: ImageView = view.findViewById(R.id.photo_fragment_image)
        val addPhoto: Button = view.findViewById(R.id.btn_add_photo)

        if (parentActivity.savedBitmap != null) {
            saveImage.setImageBitmap(parentActivity.savedBitmap)
        }

        addPhoto.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val bitmap = result.data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val resizedBitmap = ImageUtils.getResizedBitmap(bitmap, 250)
                        parentActivity.savedBitmap = resizedBitmap
                        parentActivity.userImage = ImageUtils.getStringImage(resizedBitmap)
                        saveImage.setImageBitmap(parentActivity.savedBitmap)
                    }
                }
            }


    }


    private fun checkCameraPermissionAndOpenCamera() {
        val cameraPermission = android.Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onCameraPermissionGranted()
        } else {
            requestCameraPermission.launch(cameraPermission)
        }
    }

    private fun onCameraPermissionGranted() {

        openCamera()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

}