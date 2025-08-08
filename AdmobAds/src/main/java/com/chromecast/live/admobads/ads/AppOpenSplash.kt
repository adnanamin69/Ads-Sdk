package com.chromecast.live.admobads.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.chromecast.live.admobads.ads.InterstitialAdManager.Companion.timeLapse
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

fun Activity.initAppOpenAd(adUnit: String, callBack: () -> Unit) {
    if (!isInternetConnection() || isProUser()) {
        callBack.invoke()
        return
    }


//    if (FunSolBillingHelper(this).isPremiumUser) {
//        callBack.invoke()
//        return
//    }

//    if (appOpenSplashId.isEmpty()) {
//        Handler(Looper.getMainLooper()).postDelayed({ initAppOpenAd(callBack) }, 2000)
//        return
//    }


    val request = AdRequest.Builder().build()
    val timeoutHandler = Handler(Looper.getMainLooper())
    var hasTimedOut = false

    // Create a timeout mechanism
    val timeoutRunnable = Runnable {
        hasTimedOut = true
        callBack.invoke()
    }

    timeoutHandler.postDelayed(timeoutRunnable, 8000) // Timeout after 6 seconds

    AppOpenAd.load(
        this,
        adUnit,
        request,
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {


                if (hasTimedOut) return // Do nothing if timeout already triggered
                timeoutHandler.removeCallbacks(timeoutRunnable)

                ad.show(this@initAppOpenAd)

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        callBack.invoke()
                        timeLapse = System.currentTimeMillis() + 15000
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        callBack.invoke()
                    }
                }

            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (hasTimedOut) return // Do nothing if timeout already triggered

                timeoutHandler.removeCallbacks(timeoutRunnable)
                callBack.invoke()
            }
        }
    )
}