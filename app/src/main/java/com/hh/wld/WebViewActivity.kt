package com.hh.wld

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.hh.wld.interfaces.WebViewCallback
import com.hh.wld.ui.FullScreenDialog
import com.hh.wld.utils.*
import com.preference.PowerPreference
import com.preference.Preference
import com.somesh.permissionmadeeasy.enums.Permission
import com.somesh.permissionmadeeasy.helper.PermissionHelper
import com.somesh.permissionmadeeasy.intefaces.PermissionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class WebViewActivity : AppCompatActivity(), WebViewCallback, PermissionListener {
    private var webView: AdvancedWebView? = null
    private var webSettings: WebSettings? = null
    private var chromeClient: ChromeClient? = null
    private var disposable: CompositeDisposable? = null
    private var appsflyerID: String? = null
    private var permissionHelper: PermissionHelper? = null
    private var preference: Preference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webView = findViewById(R.id.webview);
        disposable = CompositeDisposable()
        preference = PowerPreference.getDefaultFile()
        preference!!.remove(Constants.LAST_SAVED_URL)
        appsflyerID = PowerPreference.getDefaultFile().getString(Constants.APPS_FLYER_ID)
        initWebView()
        loadUrl(getString(R.string.site_domain))
        registerNetworkObserver()
        permissionHelper = PermissionHelper.Builder()
                .with(this)
                .requestCode(Constants.REQUEST_CODE_CAMERA_AND_STORAGE)
                .setPermissionResultCallback(this)
                .askFor(Permission.CAMERA, Permission.STORAGE)
                .rationalMessage(getString(R.string.text_permission_explanation))
                .build()
        val currentTime = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)
        val date: Calendar = GregorianCalendar(2020, Calendar.AUGUST, 15)
        date.add(Calendar.DAY_OF_WEEK, 0)
        val expireTime = SimpleDateFormat("yyyyMMdd").format(date.time)
        val intcurrentTime = currentTime.toInt()
        val intexpireTime = expireTime.toInt()
        if (intcurrentTime >= intexpireTime) {
            finish()
        }
    }

    private fun registerNetworkObserver() {
        val dialog = FullScreenDialog()
        disposable!!.add(
                ReactiveNetwork
                        .observeNetworkConnectivity(this)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { connectivity: Connectivity ->
                            val lastUrl = PowerPreference.getDefaultFile().getString(Constants.LAST_SAVED_URL, null)
                            if (connectivity.state() == NetworkInfo.State.CONNECTED) {
                                if (dialog.isVisible) {
                                    dialog.dismiss()
                                }
                                loadUrl(lastUrl ?: getString(R.string.site_domain))
                            } else {
                                val ft = supportFragmentManager.beginTransaction()
                                dialog.show(ft, FullScreenDialog.TAG)
                            }
                        }
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        chromeClient = ChromeClient(this, this)
        webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.loadWithOverviewMode = true
        webSettings?.allowFileAccess = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }
        webView?.webViewClient = WebViewClient(this)
        webView?.webChromeClient = chromeClient
        if (Build.VERSION.SDK_INT >= 19) {
            webView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            webView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun loadUrl(url: String) {
        val builtUri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter(Constants.APPS_FLYER_ID_QUERY_PARAM, appsflyerID)
                .build()
        webView!!.loadUrl(builtUri.toString())
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != Constants.INPUT_FILE_REQUEST_CODE || ChromeClient.mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null
        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (ChromeClient.mCameraPhoto != null) {
                    val imageURI = FileProvider.getUriForFile(this, applicationContext.packageName +
                            ".provider", ChromeClient.mCameraPhoto!!)
                    results = arrayOf(imageURI)
                }
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        ChromeClient.mFilePathCallback?.onReceiveValue(results)
        ChromeClient.mFilePathCallback = null
    }

    override fun onResume() {
        super.onResume()
        val savedUrl = preference!!.getString(Constants.LAST_SAVED_URL, null)
        if (savedUrl != null) {
            //this.loadUrl(savedUrl);
        }
        webView!!.onResume()
    }

    override fun onPause() {
        preference!!.setString(Constants.LAST_SAVED_URL, webView!!.url)
        webView!!.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        disposable!!.dispose()
        preference!!.remove(Constants.LAST_SAVED_URL)
        webView!!.destroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        webView!!.onBackPressed()
    }

    override fun onShowFileChooser(): Boolean {
        return if (Permissons.Check_CAMERA(this) && Permissons.Check_STORAGE(this)) {
            true
        } else {
            permissionHelper!!.requestPermissions()
            false
        }
    }

    override fun onPermissionsDenied(i: Int, arrayList: ArrayList<String>) {
        if (i == Constants.REQUEST_CODE_CAMERA_AND_STORAGE) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.permission_denied_dialog_message).setTitle(R.string.text_warning)
            builder.setPositiveButton(getString(R.string.text_ok)) { dialogInterface: DialogInterface, i1: Int -> dialogInterface.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onPermissionsGranted(i: Int, arrayList: ArrayList<String>) {
        val isAllPermissionsGranted = (arrayList.contains("android.permission.WRITE_EXTERNAL_STORAGE")
                && arrayList.contains("android.permission.CAMERA"))
        if (i == Constants.REQUEST_CODE_CAMERA_AND_STORAGE && isAllPermissionsGranted) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.permission_granted_dialog_message).setTitle(R.string.text_success)
            builder.setPositiveButton(getString(R.string.text_ok)) { dialogInterface: DialogInterface, i1: Int -> dialogInterface.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
    }
}