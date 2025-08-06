package com.chromecast.live.admobads.ads

import android.app.Application

abstract class AdmobApp : Application() {
    abstract fun setProdIds()
}