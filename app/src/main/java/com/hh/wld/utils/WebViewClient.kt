package com.hh.wld.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hh.wld.utils.Utils.isHostsEqual
import com.preference.PowerPreference
import com.preference.Preference

class WebViewClient(private val context: Context) : WebViewClient() {
    private var progressDialog: ProgressDialog? = null
    private val preference: Preference
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        // If url don't contains query param "did"
        // and last saved url is empty
        // then save this url as main domain
        val savedUrl = preference.getString(Constants.LAST_SAVED_URL, null)
        if (!url.contains("did=") && savedUrl == null) {
            preference.setString(Constants.LAST_SAVED_URL, url)
        }

        // if last host is the same as new one
        // the open this url in app
        // else open in default browser
        if (isHostsEqual(savedUrl, url)) {
            view.loadUrl(url)
        } else {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        return true
    }

    //Show loader on url load
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        // Then show progress  Dialog
        // in standard case YourActivity.this
        if (progressDialog == null) {
            progressDialog = ProgressDialog(context)
            progressDialog!!.setMessage("Loading...")
            progressDialog!!.show()
        }
    }

    // Called when all page resources loaded
    override fun onPageFinished(view: WebView, url: String) {
        try {
            // Close progressDialog
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
                progressDialog = null
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    init {
        preference = PowerPreference.getDefaultFile()
    }
}