package com.chromecast.live.admobads.ads

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chromecast.live.admobads.R
import com.chromecast.live.admobads.databinding.BannerFrameBinding

import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.sebaslogen.resaca.rememberScoped

private const val TAG = "BannerUtils"


/**
 * Composable function for displaying a banner ad with optional collapsible functionality
 *
 * @param modifier Compose modifier for styling and layout
 * @param adUnit The AdMob banner ad unit ID
 * @param collapsible Optional parameter for collapsible banner ads: "top", "bottom", or null
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnit: String,
    collapsible: String? = null,
) {
    val context = LocalActivity.current

    val binding = rememberScoped {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.banner_frame, null, false)
            .let { view -> BannerFrameBinding.bind(view) }

        view

    }


    AndroidView(
        modifier = modifier
            .fillMaxWidth(),
        factory = {
            binding.root
        }// Only uses the remembered binding
    )

    // This ensures loadBanner is called only once per adUnit change
    LaunchedEffect(adUnit, collapsible) {
        context?.loadBanner(adUnit, binding.root, collapsible)
    }
}


/**
 * Loads a banner ad into the specified FrameLayout
 *
 * @param adUnit The AdMob ad unit ID for the banner ad
 * @param frameLayout The FrameLayout container where the banner ad will be displayed
 * @param collapsible Optional parameter to create collapsible banner ads:
 *   - "top": The top of the expanded ad aligns to the top of the collapsed ad (ad placed at top of screen)
 *   - "bottom": The bottom of the expanded ad aligns to the bottom of the collapsed ad (ad placed at bottom of screen)
 *   - null or omitted: Regular banner ad (non-collapsible)
 *
 * Collapsible banner ads expand when clicked and collapse when dismissed, providing a better user experience
 * by not taking up permanent screen space while still being easily accessible.
 */

fun Activity.loadBanner(
    adUnit: String,
    frameLayout: FrameLayout,
    collapsible: String? = null
) {
    Log.i(TAG, "loadBanner: $adUnit")
    frameLayout.visibility = View.VISIBLE
    val shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_container)

    if (!isNetworkAvailable(this) || isProUser()) {
        shimmerLayout?.visibility = View.GONE
        frameLayout.visibility = View.GONE
        return
    }


    shimmerLayout?.startShimmer()

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

    // Set AdListener to handle ad events
    adView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            // Stop shimmer and hide it when ad is loaded
            shimmerLayout?.stopShimmer()
            shimmerLayout?.visibility = View.GONE

            frameLayout.removeAllViews()
            frameLayout.addView(adView)

            adView.visibility = View.VISIBLE
            Log.i(TAG, "onAdLoaded: ")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            // Keep shimmer visible if ad fails to load
            shimmerLayout?.visibility = View.VISIBLE
        }
    }


    val extras = Bundle()
    extras.putString("collapsible", collapsible)


    // Load the ad
    val adRequest =
        if (collapsible != null)
            AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        else AdRequest.Builder()
            .build()

    adView.loadAd(adRequest)
}
