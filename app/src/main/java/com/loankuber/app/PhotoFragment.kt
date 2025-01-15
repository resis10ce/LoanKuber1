package com.loankuber.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.loankuber.app.utils.ImageUtils
import com.loankuber.library.permissions.CameraPermissionHandler
import com.loankuber.library.utils.KotlinUtils.toast

/**
 * Summary:
 * once the add clicks the camera button than it checks the camera permission and open the camera if granted
 * cameraLauncher receive the result once the user click  the photo
*/
class PhotoFragment : Fragment(R.layout.fragment_photo) {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val cameraPermissionHandler = CameraPermissionHandler(this)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parentActivity = requireActivity() as DetailActivity

        // Initialize the views
        val saveImage: ImageView = view.findViewById(R.id.photo_fragment_image)
        val addPhoto: Button = view.findViewById(R.id.btn_add_photo)

        // If the user has already clicked a photo, then show it here
        if (parentActivity.savedBitmap != null) {
            saveImage.setImageBitmap(parentActivity.savedBitmap)
        }

        addPhoto.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        /*
        cameraLauncher is used to launch the camera intent and receive the result once the user clicks the photo
         */
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
        if(cameraPermissionHandler.isPermissionGranted(requireContext())){
            onCameraPermissionGranted()
        }
        else{
            cameraPermissionHandler.requestPermission { isGranted ->
                if (isGranted) {
                    onCameraPermissionGranted()
                } else {
                    toast("Camera permission denied")
                }
            }
        }
    }

    private fun onCameraPermissionGranted() {
        // Open camera intent for the user to click a photo
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

}