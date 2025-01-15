package com.loankuber.library.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

object KotlinUtils {
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Fragment.toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}