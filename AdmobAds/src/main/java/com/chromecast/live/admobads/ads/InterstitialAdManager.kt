package com.chromecast.live.admobads.ads

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.activity.ComponentActivity
import com.chromecast.live.admobads.R
import com.chromecast.live.admobads.databinding.DialogAdLoadingBinding

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

class InterstitialAdManager {

    companion object {
        private const val TAG = "InterstitialAdManager"
        private const val AD_TIMEOUT_MS = 30000L // 30 seconds
        private var clickCount = 0
        var timeLapse = 0L
        //  private const val CLICKS_BEFORE_SHOW = 3
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = AtomicBoolean(false)
    private var isAdReady = AtomicBoolean(false)
    private var loadingDialog: Dialog? = null
    private var timeoutHandler: Handler? = null
    private var timeoutRunnable: Runnable? = null
    private var currentAdUnitId: String? = null
    private var onAdDismissedCallback: ((Boolean) -> Unit)? = null
    private var showLoading: Boolean = true

    /**
     * Load and show interstitial ad with all the specified requirements
     * @param context Activity context
     * @param adUnitId The ad unit ID to load
     * @param onAdDismissed Callback when ad is dismissed
     * @param onAdFailed Callback when ad fails to load or show
     */
    fun loadAndShowAd(
        context: Context,
        adUnitId: String,
        clickIntervals: Int = 1,
        showLoading: Boolean = true,
        enableTimeLapse: Boolean = true,
        onAdDismissed: ((Boolean) -> Unit)? = null,
    ) {


        this.onAdDismissedCallback = onAdDismissed
        this.showLoading = showLoading



        Log.i(
            TAG,
            "loadAndShowAd: $clickCount : : : $clickIntervals : : : ${clickCount % clickIntervals}"
        )

        val timeCheck = timeLapse > System.currentTimeMillis()

        if (!timeCheck)
            clickCount++


        // Check if we should show ad immediately (every 3rd click)
        if (clickCount % clickIntervals != 0 || (timeCheck && enableTimeLapse)) {
            onAdDismissed?.invoke(false)
            return
        }


        // If ad is already loading, just perform the action
        if (isLoading.get()) {
            Log.d(TAG, "Ad is already loading, performing action immediately")
            // onAdDismissed?.invoke()
            return
        }

        // If ad is ready and same ad unit, show it
        if (isAdReady.get() && interstitialAd != null) {
            showAd(context)
            return
        }


        // Load new ad
        loadAd(
            context, adUnitId
        )
    }

    private fun loadAd(context: Context, adUnitId: String) {
        if (isLoading.getAndSet(true)) {
            return
        }

        currentAdUnitId = adUnitId
        isAdReady.set(false)

        // Show loading dialog
        if (showLoading)
            showLoadingDialog(context)

        // Set timeout
        setupTimeout()

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    isLoading.set(false)
                    isAdReady.set(true)
                    interstitialAd = ad

                    // Cancel timeout since ad loaded successfully
                    cancelTimeout()

                    // Hide loading dialog
                    hideLoadingDialog()

                    // Set up ad callbacks
                    setupAdCallbacks(context)

                    // Show ad immediately
                    showAd(context)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    isLoading.set(false)
                    isAdReady.set(false)
                    interstitialAd = null

                    // Cancel timeout
                    cancelTimeout()

                    // Hide loading dialog
                    hideLoadingDialog()

                    // Perform failed callback
                    onAdDismissedCallback?.invoke(false)
                }
            }
        )
    }

    private fun setupAdCallbacks(context: Context) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad was dismissed")
                isAdReady.set(false)
                interstitialAd = null
                timeLapse = System.currentTimeMillis() + 15000
                // Perform dismissed callback
                onAdDismissedCallback?.invoke(true)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                isAdReady.set(false)
                interstitialAd = null

                // Perform failed callback
                onAdDismissedCallback?.invoke(false)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed full screen content")
            }
        }
    }

    private fun showAd(context: Context) {
        if (context is ComponentActivity && !context.isFinishing && !context.isDestroyed) {
            interstitialAd?.show(context)
        } else {
            Log.e(TAG, "Context is not valid for showing ad")
            onAdDismissedCallback?.invoke(false)
        }
    }

    private fun showLoadingDialog(context: Context) {
        if (loadingDialog?.isShowing == true) {
            return
        }

        try {
            loadingDialog = Dialog(context, R.style.FullScreenDialog).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)

                val binding = DialogAdLoadingBinding.inflate(LayoutInflater.from(context))
                setContentView(binding.root)

                // Set loading text
                binding.tvLoadingText.text = context.getString(R.string.ad_is_loading)
            }

            loadingDialog?.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading dialog", e)
        }
    }

    private fun hideLoadingDialog() {
        try {
            loadingDialog?.dismiss()
            loadingDialog = null
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding loading dialog", e)
        }
    }

    private fun setupTimeout() {
        timeoutHandler = Handler(Looper.getMainLooper())
        timeoutRunnable = Runnable {
            Log.w(TAG, "Ad loading timeout after $AD_TIMEOUT_MS ms")
            isLoading.set(false)
            hideLoadingDialog()

            onAdDismissedCallback?.invoke(false)

        }

        timeoutHandler?.postDelayed(timeoutRunnable!!, AD_TIMEOUT_MS)
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { runnable ->
            timeoutHandler?.removeCallbacks(runnable)
        }
        timeoutHandler = null
        timeoutRunnable = null
    }

    /**
     * Check if an ad is ready to show
     */
    fun isAdReady(): Boolean {
        return isAdReady.get() && interstitialAd != null
    }

    /**
     * Check if an ad is currently loading
     */
    fun isLoading(): Boolean {
        return isLoading.get()
    }

    /**
     * Get current click count
     */
    fun getClickCount(): Int {
        return clickCount
    }

    /**
     * Reset click count (useful for testing or specific scenarios)
     */
    fun resetClickCount() {
        clickCount = 0
    }

    /**
     * Clear current ad and reset state
     */
    fun clearAd() {
        interstitialAd = null
        isAdReady.set(false)
        isLoading.set(false)
        currentAdUnitId = null
        cancelTimeout()
        hideLoadingDialog()
    }
} 