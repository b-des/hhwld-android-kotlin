package com.hh.wld.utils

import android.content.Context
import android.net.Uri
import com.google.common.net.InternetDomainName
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

object Utils {
    /**
     * Create image file in public directory
     * @return
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val imageStorageDir = File(context.filesDir.absolutePath)
        if (!imageStorageDir.exists()) {
            // Create folder at sdcard
            imageStorageDir.mkdirs()
        }
        return File(imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg")
    }

    @JvmStatic
    fun isHostsEqual(url1: String?, url2: String?): Boolean {
        if (url1 == null || url2 == null) {
            return true
        }
        val uri1 = Uri.parse(url1)
                .buildUpon()
                .build()
        val uri2 = Uri.parse(url2)
                .buildUpon()
                .build()
        val host1 = InternetDomainName.from(uri1.host).topDomainUnderRegistrySuffix().toString()
        val host2 = InternetDomainName.from(uri2.host).topDomainUnderRegistrySuffix().toString()
        return host1 == host2
    }
}