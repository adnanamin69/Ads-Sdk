package com.adnan.live.adssdk

import android.app.Application
import com.chromecast.live.admobads.ads.AppOpenAdManager

class MyApp : Application() {
    val appOpenManger: AppOpenAdManager by lazy {
        AppOpenAdManager(this, "ca-app-pub-3940256099942544/9257395921")
    }

    override fun onCreate() {
        super.onCreate()
        appOpenManger.forceLoadAd()

    }
}
