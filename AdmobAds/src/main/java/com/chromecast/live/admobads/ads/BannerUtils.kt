package com.chromecast.live.admobads.ads

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.chromecast.live.admobads.R

import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "BannerUtils"
fun Activity.loadBanner(
    adUnit: String,
    frameLayout: FrameLayout,
) {
    Log.i(TAG, "loadBanner: ")
    frameLayout.visibility = View.VISIBLE
    val shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_container)

    if (!isNetworkAvailable(this) || isProUser()) {
        shimmerLayout.visibility = View.GONE
        frameLayout.visibility = View.GONE
        return
    }



    shimmerLayout.stopShimmer()

    // Initialize AdView
    val adView = AdView(this)
    adView.adUnitId = adUnit // Test Ad Unit ID

    val display = windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)

    val density = outMetrics.density
    var adWidthPixels = frameLayout.width.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }

    val adWidth = (adWidthPixels / density).toInt()
    val size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)


    adView.setAdSize(size)
    frameLayout.addView(adView)

    // Set AdListener to handle ad events
    adView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            // Stop shimmer and hide it when ad is loaded
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            adView.visibility = View.VISIBLE
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            // Keep shimmer visible if ad fails to load
            shimmerLayout.visibility = View.VISIBLE
        }
    }

    // Load the ad
    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)
}
