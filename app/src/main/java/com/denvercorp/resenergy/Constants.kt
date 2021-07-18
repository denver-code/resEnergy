package com.denvercorp.resenergy

import android.Manifest


object Constants {

    const val TAG = "den"
    const val FILE_NAME_FORMAT = "dd-MM-yyyy"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
//    , Manifest.permission.WRITE_EXTERNAL_STORAGE
}