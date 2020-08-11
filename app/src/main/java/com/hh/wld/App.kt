package com.hh.wld

import android.app.Application
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.hh.wld.utils.Constants
import com.preference.PowerPreference
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : Application() {
    private var AF_DEV_KEY = ""
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        PowerPreference.init(this)
        val preference = PowerPreference.getDefaultFile()
        AF_DEV_KEY = getString(R.string.appslyer_key)
        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {}
            override fun onConversionDataFail(errorMessage: String) {}
            override fun onAppOpenAttribution(attributionData: Map<String, String>) {}
            override fun onAttributionFailure(errorMessage: String) {}
        }
        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this)
        if (!preference.contains(Constants.APPS_FLYER_ID)) {
            val appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(this)
            preference.setString(Constants.APPS_FLYER_ID, appsFlyerId)
        }
    }
}