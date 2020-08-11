package com.hh.wld.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.content.FileProvider
import com.hh.wld.R
import com.hh.wld.interfaces.WebViewCallback
import com.hh.wld.utils.Utils.createImageFile
import com.hh.wld.utils.Utils.isHostsEqual
import com.preference.PowerPreference
import timber.log.Timber
import java.io.File
import java.io.IOException

class ChromeClient(private val activity: Activity, var callback: WebViewCallback) : WebChromeClient() {

    // save current URL
    // because some sites don't use HTTP redirect(i.e. Angular, and other SPA)
    // need to observe some changes in different way
    // so onProgressChanged method is good place to handle that changes
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        //save url only when page loaded
        if (newProgress == 100) {
            val savedUrl = PowerPreference.getDefaultFile().getString(Constants.LAST_SAVED_URL, null)
            if (savedUrl != null && isHostsEqual(savedUrl, view.url)) {
                PowerPreference.getDefaultFile().setString(Constants.LAST_SAVED_URL, view.url)
            }
        }
    }

    override fun onShowFileChooser(view: WebView, filePath: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
        if (!callback.onShowFileChooser()) {
            return false
        }
        // Double check that we don't have any existing callbacks
        if (mFilePathCallback != null) {
            mFilePathCallback!!.onReceiveValue(null)
        }
        mFilePathCallback = filePath
        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent!!.resolveActivity(activity.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile(activity)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Timber.d(ex, "Unable to create Image File")
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhoto = photoFile
                val outputFileUri = FileProvider.getUriForFile(activity, activity.applicationContext.packageName + ".provider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                takePictureIntent = null
            }
        }
        // create intent to open image chooser
        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "image/*"
        val intentArray: Array<Intent?>
        intentArray = takePictureIntent.let { arrayOf(it) } ?: arrayOfNulls<Intent>(0)
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, activity.getString(R.string.text_image_chooser))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        activity.startActivityForResult(chooserIntent, Constants.INPUT_FILE_REQUEST_CODE)
        return true
    }

    companion object {
        private const val TAG = "WEBVIEW"
        var mUploadMessage: ValueCallback<Uri>? = null
        var mCapturedImageURI: Uri? = null
        var mFilePathCallback: ValueCallback<Array<Uri>>? = null
        var mCameraPhoto: File? = null
    }

}