package com.hh.wld.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissons {
    fun Check_CAMERA(act: Activity?): Boolean {
        val result = ContextCompat.checkSelfPermission(act!!, Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun Check_STORAGE(act: Activity?): Boolean {
        val result = ContextCompat.checkSelfPermission(act!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun Request_CAMERA(act: Activity?, code: Int) {
        ActivityCompat.requestPermissions(act!!, arrayOf(Manifest.permission.CAMERA), code)
    }

    fun Request_STORAGE(act: Activity?, code: Int) {
        ActivityCompat.requestPermissions(act!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), code)
    }
}