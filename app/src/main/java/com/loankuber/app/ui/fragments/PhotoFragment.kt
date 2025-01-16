package com.loankuber.app.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
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
        val continueBtn: Button = view.findViewById(R.id.continue_btn)
        val addPhotoLayout: LinearLayout = view.findViewById(R.id.add_photo_layout)

        // If the user has already clicked a photo, then show it here
        if (parentActivity.savedBitmap != null) {
            saveImage.setImageBitmap(parentActivity.savedBitmap)
            addPhotoLayout.visibility = View.GONE
        }

        saveImage.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        continueBtn.setOnClickListener {
            if(parentActivity.userImage != null)
                parentActivity.showNextFragment()
            else
                toast("Please click a photo")
        }

        /*
        cameraLauncher is used to launch the camera intent and receive the result once the user clicks the photo
         */
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val bitmap = result.data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val resizedBitmap = ImageUtils.getResizedBitmap(bitmap, 300)
                        parentActivity.savedBitmap = resizedBitmap
                        parentActivity.userImage = ImageUtils.getStringImage(resizedBitmap)
                        saveImage.setImageBitmap(bitmap)
                        addPhotoLayout.visibility = View.GONE

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