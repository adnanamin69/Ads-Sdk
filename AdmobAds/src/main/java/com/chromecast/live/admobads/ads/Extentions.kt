package com.chromecast.live.admobads.ads

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.edit

fun Context.isInternetConnection(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}


private const val PREF_NAME = "user_prefs"
private const val KEY_IS_PRO = "is_pro_user"

// Save Pro status
fun Context.saveProUser(isPro: Boolean) {
    val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    prefs.edit { putBoolean(KEY_IS_PRO, isPro) }
}

// Get Pro status
fun Context.isProUser(): Boolean {
    val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_IS_PRO, false)
}