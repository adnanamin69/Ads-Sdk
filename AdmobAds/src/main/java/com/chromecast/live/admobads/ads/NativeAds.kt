package com.chromecast.live.admobads.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.chromecast.live.admobads.R
import com.chromecast.live.admobads.databinding.NativeFrameBigBinding
import com.chromecast.live.admobads.databinding.NativeFrameSmallBinding

import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.sebaslogen.resaca.rememberScoped


/**
 * Composable function for displaying a medium-sized native ad
 *
 * @param modifier Compose modifier for styling and layout
 * @param unitId The AdMob native ad unit ID
 */
@Composable
fun NativeMedium(
    modifier: Modifier = Modifier,
    unitId: String,
    withFailedMedia: (() -> Unit)? = null
) {
    val context = LocalActivity.current
    val binding = rememberScoped("ad") {

        val view = LayoutInflater.from(context)
            .inflate(R.layout.native_frame_big, null, false)
            .let { view -> NativeFrameBigBinding.bind(view) }
        context?.nativeAdMedium(view.adFrameNative, unitId, withFailedMedia)

        view

    }

    AndroidView(
        modifier = modifier,
        factory = {
            binding.root
        }// Only uses the remembered binding
    )
}


/**
 * Composable function for displaying a small-sized native ad
 *
 * @param modifier Compose modifier for styling and layout
 * @param unitId The AdMob native ad unit ID
 */
@Composable
fun NativeSmall(
    modifier: Modifier = Modifier, unitId: String,
    withFailed: (() -> Unit)? = null
) {
    val context = LocalActivity.current
    val binding = rememberScoped("ad") {
        val view = LayoutInflater.from(context)
            .inflate(com.chromecast.live.admobads.R.layout.native_frame_small, null, false)
            .let { view -> NativeFrameSmallBinding.bind(view) }
        context?.nativeAdMainSmall(view.adFrameNative, unitId, withFailed)

        view

    }

    AndroidView(
        modifier = modifier,
        factory = {
            binding.root
        }
        // Only uses the remembered binding
    )
}


private const val TAG = "AdUtilss"

/**
 * Loads a small native ad into the specified FrameLayout
 *
 * @param frameAd The FrameLayout container where the small native ad will be displayed
 * @param adUnit The AdMob native ad unit ID
 *
 * Small native ads are text-only ads without media content, suitable for compact layouts.
 */
fun Context.nativeAdMainSmall(
    frameAd: FrameLayout,
    adUnit: String,
    withFailed: (() -> Unit)? = null
) {


    val adView = LayoutInflater.from(this).inflate(R.layout.native_frame_small, null, false)
    frameAd.removeAllViews()
    frameAd.addView(adView)


    val txtAd = frameAd.findViewById<ShimmerFrameLayout>(R.id.shimmer_container_native)

    if (!isNetworkAvailable(this) || isProUser()) {
        txtAd.visibility = View.GONE
        frameAd.visibility = View.GONE
        return
    }


    val builder = AdLoader.Builder(this, adUnit)
    Log.i(TAG, "nativeAdMainSmall: adnan $adUnit")
    builder.forNativeAd { NativeAd ->

//            if (NativeAd != null) nativeAd = NativeAd
        // Access the LayoutInflater using the activity context
        if (this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) != null) {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            // Check again if the Fragment is attached before proceeding
            val adView = inflater.inflate(R.layout.native_banner, null, false) as NativeAdView
            try {
                populateUnifiedNativeAdViewSmall(NativeAd, adView)
            } catch (e: Exception) {
            }
            frameAd.removeAllViews()
            frameAd.addView(adView)
        }

    }

    val adLoader = builder.withAdListener(object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            txtAd.visibility = View.GONE
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            Log.d(TAG, "onAdFailedToLoad: ${p0.message}")
            frameAd.visibility = View.GONE
            withFailed?.invoke()
        }
    }).build()

    val adRequest = AdRequest.Builder().build()
    adLoader.loadAd(adRequest)

}

/**
 * Populates a small native ad view with ad content
 *
 * @param nativeAd The NativeAd object containing the ad data
 * @param adView The NativeAdView to populate with ad content
 *
 * This function sets up the small native ad layout with headline, body text, call-to-action button,
 * and app icon. It handles cases where certain ad assets might be missing.
 */
fun populateUnifiedNativeAdViewSmall(
    nativeAd: NativeAd,
    adView: NativeAdView,
) {

    // Set the media view.
//        adView.mediaView = adView.findViewById(R.id.ad_media)

    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.mediaView = adView.findViewById(R.id.ad_media)
//        adView.priceView = adView.findViewById(R.id.ad_price)
//        adView.starRatingView = adView.findViewById(R.id.ad_stars)
//        adView.storeView = adView.findViewById(R.id.ad_store)
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    // The headline and media content are guaranteed to be in every UnifiedNativeAd.
    (adView.headlineView as TextView).text = nativeAd.headline
//        adView.mediaView?.setMediaContent(nativeAd.mediaContent)

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as TextView).text = nativeAd.callToAction
    }



    if (nativeAd.mediaContent != null && nativeAd.mediaContent != null) {
        Log.i(TAG, "populateUnifiedNativeAdViewSmall: media")
        adView.mediaView?.visibility = View.VISIBLE
        adView.iconView?.visibility = View.INVISIBLE
        adView.mediaView?.mediaContent = nativeAd.mediaContent

    } else if (nativeAd.icon != null) {
        Log.i(TAG, "populateUnifiedNativeAdViewSmall: icon")
        (adView.iconView as ImageView).setImageDrawable(
            nativeAd.icon?.drawable
        )
        adView.iconView?.visibility = View.VISIBLE
        adView.mediaView?.visibility = View.INVISIBLE
    } else {
        Log.i(TAG, "populateUnifiedNativeAdViewSmall: nothing")
        adView.iconView?.visibility = View.INVISIBLE
        adView.mediaView?.visibility = View.INVISIBLE
    }




    adView.setNativeAd(nativeAd)
}


/**
 * Loads a medium native ad into the specified FrameLayout
 *
 * @param frameLayout The FrameLayout container where the medium native ad will be displayed
 * @param adUnit The AdMob native ad unit ID
 *
 * Medium native ads include media content (images/videos) along with text elements,
 * providing a richer advertising experience compared to small native ads.
 */
fun Activity.nativeAdMedium(
    frameLayout: FrameLayout,
    adUnit: String,
    withFailed: (() -> Unit)? = null
) {


    val adView = LayoutInflater.from(this).inflate(R.layout.native_frame_big, null, false)
    frameLayout.removeAllViews()
    frameLayout.addView(adView)


    val context = this
    val shimmerFrameLayout =
        frameLayout.findViewById<ShimmerFrameLayout>(R.id.shimmer_container_native)
    if (!isNetworkAvailable(this) || isProUser()) {
        shimmerFrameLayout.visibility = View.GONE
        frameLayout.visibility = View.GONE
        return
    }


    val builder = AdLoader.Builder(this, adUnit)

    builder.forNativeAd { NativeAd ->
        val adView = layoutInflater.inflate(R.layout.custom_native_ad, null, false) as NativeAdView
        if (isDestroyed || isFinishing || isChangingConfigurations) {
            NativeAd.destroy()
            return@forNativeAd
        }
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.

        try {
            populateUnifiedNativeAd(NativeAd, adView)
        } catch (e: Exception) {
        }

        frameLayout.removeAllViews()
        frameLayout.addView(adView)
    }

    val adLoader = builder.withAdListener(object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            shimmerFrameLayout.visibility = View.GONE
            Log.i(TAG, "onAdLoaded: ")
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            shimmerFrameLayout.visibility = View.GONE
            frameLayout.visibility = View.GONE
            withFailed?.invoke()
        }
    }).build()

    val adRequest = AdRequest.Builder().build()
    adLoader.loadAd(adRequest)

}

/**
 * Populates a medium native ad view with ad content including media
 *
 * @param nativeAd The NativeAd object containing the ad data
 * @param adView The NativeAdView to populate with ad content
 *
 * This function sets up the medium native ad layout with headline, body text, call-to-action button,
 * app icon, media content (images/videos), and advertiser information. It handles cases where
 * certain ad assets might be missing.
 */
fun Activity.populateUnifiedNativeAd(
    nativeAd: NativeAd,
    adView: NativeAdView
) {


    // Set the media view.
    adView.mediaView = adView.findViewById(R.id.ad_media)

    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)

//        adView.storeView = adView.findViewById(R.id.ad_store)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    // The headline and media content are guaranteed to be in every UnifiedNativeAd.
    (adView.headlineView as TextView).text = nativeAd.headline


    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as TextView).text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        adView.iconView?.visibility = View.INVISIBLE
    } else {
        (adView.iconView as ImageView).setImageDrawable(
            nativeAd.icon?.drawable
        )
        adView.iconView?.visibility = View.VISIBLE
    }
    adView.mediaView?.mediaContent = nativeAd.mediaContent


    if (nativeAd.advertiser == null) {
        adView.advertiserView?.visibility = View.INVISIBLE
    } else {
        (adView.advertiserView as TextView).text = nativeAd.advertiser
        adView.advertiserView?.visibility = View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad.
    adView.setNativeAd(nativeAd)
}


/**
 * Checks if the device has an active network connection
 *
 * @param context The context to access system services
 * @return true if network is available and connected, false otherwise
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.getActiveNetworkInfo()
    return activeNetworkInfo != null && activeNetworkInfo.isConnected()
}