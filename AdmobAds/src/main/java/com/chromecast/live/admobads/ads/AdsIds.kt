package com.chromecast.live.admobads.ads

// App Open Ad - Show immediately after app is launched
var appOpen = ""
var appOpenSplash = ""
var splashInterstitial = ""
var homeInterstitial = ""
var banner = ""
var nativeCard = ""

fun setTestIds() {

    val bannerIdTest = "ca-app-pub-3940256099942544/6300978111" //test
    val appOpenIdTest = "ca-app-pub-3940256099942544/9257395921"//test
    val interstitialIdTest = "ca-app-pub-3940256099942544/1033173712"//test
    //  val appIdTest = "ca-app-pub-3940256099942544~3347511713"//test
    val nativeIdTest = "ca-app-pub-3940256099942544/2247696110"//test

    appOpen = appOpenIdTest
    appOpenSplash = appOpenIdTest
    splashInterstitial = interstitialIdTest
    homeInterstitial = interstitialIdTest
    banner = bannerIdTest
    nativeCard = nativeIdTest

}



