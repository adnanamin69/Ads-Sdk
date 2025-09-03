package com.chromecast.live.admobads.ads

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.lifecycle.LifecycleObserver
import com.chromecast.live.admobads.R
import com.chromecast.live.admobads.ads.InterstitialAdManager.Companion.timeLapse
import com.chromecast.live.admobads.databinding.DialogAdLoadingBinding
import com.chromecast.live.admobads.ads.InterstitialAdManager.Companion.isFullscreenAdShowing

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

import java.util.Date
import kotlin.apply
import kotlin.collections.any
import kotlin.let

class AppOpenAdManager(val context: Application, val adUnit: String) :
    Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    companion object {
        private const val TAG = "AppOpenAdManager"
        private const val AD_TIMEOUT_HOURS = 4L // 4 hours timeout
        private const val AD_LOAD_TIMEOUT_MS = 10000L // 10 seconds timeout for loading
        private const val LOADING_DIALOG_DURATION = 3000L // 3 seconds
        var showAppOpenAd: Boolean = false
        var doNotShowAppOpen = false
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private var isAppInForeground = false
    private var loadingDialog: Dialog? = null
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    init {
        context.registerActivityLifecycleCallbacks(this)
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(AD_TIMEOUT_HOURS)
    }

    /** Load an ad. */
    fun loadAd() {
        // Don't load ad if there's already an ad loading or if ad is disabled by remote config


        if (context.isProUser()) {
            Log.d(TAG, "User has Premium subscription")
            return
        }


        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            adUnit,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded successfully")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "App open ad failed to load: ${loadAdError.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    /** Show the ad if one isn't already showing. */
    fun showAdIfAvailable(activity: Activity) {
        // If the app open ad is already showing, do not show the ad again.

        if (activity.isProUser()) {
            Log.d(TAG, "User has Premium subscription")
            return
        }


        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        // Do not show app open ad if an interstitial/rewarded fullscreen ad is showing
        if (isFullscreenAdShowing.get()) {
            Log.d(TAG, "Skipping app open ad because another fullscreen ad is showing.")
            return
        }


        // If the ad is not available, don't show it.
        if (!isAdAvailable()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            loadAd()
            return
        }


        // Only show ad if app is coming to foreground
        if (!isAppInForeground) {
            Log.d(TAG, "App is not in foreground, skipping app open ad")
            return
        }



        if (doNotShowAppOpen) {
            Log.d(TAG, "Do not show app open")
            doNotShowAppOpen = false
            return

        }


        // Show loading dialog for 3 seconds before showing the ad
        showLoadingDialog(activity)
    }

    /** Show loading dialog for 3 seconds */
    private fun showLoadingDialog(activity: Activity) {
        if (loadingDialog?.isShowing == true) {
            return
        }

        try {
            loadingDialog = Dialog(activity, R.style.FullScreenDialog).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                val binding = DialogAdLoadingBinding.inflate(LayoutInflater.from(activity))
                setContentView(binding.root)

                // Update the loading text to show welcome message
                binding.tvLoadingText.text = "Welcome back!\nLoading ad..."
            }

            loadingDialog?.show()

            // Schedule showing the ad after 3 seconds
            loadingHandler = Handler(Looper.getMainLooper())
            loadingRunnable = Runnable {
                hideLoadingDialog()
                showAppOpenAd(activity)
            }

            loadingHandler?.postDelayed(loadingRunnable!!, LOADING_DIALOG_DURATION)

        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading dialog", e)
            // If dialog fails, show ad immediately
            showAppOpenAd(activity)
        }
    }

    /** Hide loading dialog */
    private fun hideLoadingDialog() {
        try {
            loadingDialog?.dismiss()
            loadingDialog = null

            // Cancel the delayed runnable
            loadingRunnable?.let { runnable ->
                loadingHandler?.removeCallbacks(runnable)
            }
            loadingHandler = null
            loadingRunnable = null
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding loading dialog", e)
        }
    }

    /** Show the actual app open ad */
    private fun showAppOpenAd(activity: Activity) {
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Set the reference to null so isAdAvailable() returns false.
                appOpenAd = null
                isShowingAd = false
                Log.d(TAG, "App open ad was dismissed.")
                loadAd()
                timeLapse = System.currentTimeMillis() + 15000

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "App open ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                Log.d(TAG, "App open ad showed fullscreen content.")
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    /** Manually trigger ad loading (useful for testing or specific scenarios) */
    fun forceLoadAd() {
        Log.d(TAG, "Force loading app open ad")
        loadAd()
    }

    /** Check if app open ad is currently loading */
    fun isAdLoading(): Boolean = isLoadingAd

    /** Check if app open ad is available to show */
    fun isAdReady(): Boolean = isAdAvailable()

    // ActivityLifecycleCallbacks implementation
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Not used
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        isAppInForeground = true

    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        isAppInForeground = true
        

        if (showAppOpenAd) {
            showAdIfAvailable(activity)
            showAppOpenAd = false
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Not used
    }

    override fun onActivityPaused(activity: Activity) {
        // Not used
    }

    override fun onActivityStopped(activity: Activity) {

        if (!isAppInForeground()) {
            Log.d("AppState", "App went to background")
            showAppOpenAd = true
        }
        // Check if app is going to background
        if (currentActivity == activity) {
            isAppInForeground = false
            // Hide loading dialog if app goes to background
            hideLoadingDialog()
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
            // Hide loading dialog if activity is destroyed
            hideLoadingDialog()
        }
    }


    private fun isAppInForeground(): Boolean {
        if (context.isProUser())
            return false

        val activityManager =
            currentActivity?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return false // cannot get ActivityManager, assume app is not in foreground

        val runningProcesses = activityManager.runningAppProcesses ?: return false
        return runningProcesses.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

} 