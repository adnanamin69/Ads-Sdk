package com.adnan.live.adssdk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.adnan.live.adssdk.ui.theme.AdsSdkTheme
import com.chromecast.live.admobads.ads.BannerAd
import com.chromecast.live.admobads.ads.GoogleMobileAdsConsentManager
import com.chromecast.live.admobads.ads.InterstitialAdManager
import com.chromecast.live.admobads.ads.NativeMedium
import com.chromecast.live.admobads.ads.NativeSmall
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager by lazy {
        GoogleMobileAdsConsentManager(this)
    }

    val adManager: InterstitialAdManager by lazy {
        InterstitialAdManager()
    }


    private suspend fun initializeConsent() {
        withContext(Dispatchers.IO) {

            googleMobileAdsConsentManager.gatherConsent(this@MainActivity) { consentError ->
                if (googleMobileAdsConsentManager.canRequestAds) {
                    MobileAds.initialize(this@MainActivity) {}
                    Log.i("AppOpenAdManager", "SplashScreen: ")

                    /*         //  activity.nativeAdMedium(binding.nativeView.adFrameNative, nativeCard)
                             initAppOpenAd("ca-app-pub-3940256099942544/9257395921") {

                                 Toast.makeText(
                                     this@MainActivity,
                                     "perform action after app open start",
                                     Toast.LENGTH_SHORT
                                 ).show()
                             }
         */
                    /*adManager.loadAndShowAd(activity, splashInterstitial, 1, false) {
                        if (it) {
                            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                        }
                    }*/

                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            initializeConsent()
        }
        setContent {
            AdsSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(

                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(innerPadding),

                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        Button({

                            adManager.loadAndShowAd(
                                this@MainActivity,
                                "ca-app-pub-3940256099942544/1033173712",
                                clickIntervals = 1,
                                showLoading = false
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "perform your actio ad is showed $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {

                            Text("Show Interstitial without loading")
                        }


                        Button({

                            adManager.loadAndShowAd(
                                this@MainActivity,
                                "ca-app-pub-3940256099942544/1033173712",
                                clickIntervals = 1,
                                enableTimeLapse = false,
                                showLoading = false
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "perform your actio ad is showed $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {

                            Text("Show Interstitial without loading time lapse off")
                        }


                        Button({

                            adManager.loadAndShowAd(
                                this@MainActivity,
                                "ca-app-pub-3940256099942544/1033173712",
                                clickIntervals = 1,
                                showLoading = true
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "perform your actio ad is showed $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {

                            Text("Show Interstitial on every clicks")
                        }


                        Button({

                            adManager.loadAndShowAd(
                                this@MainActivity,
                                "ca-app-pub-3940256099942544/1033173712",
                                clickIntervals = 1,
                                enableTimeLapse = false,
                                showLoading = true
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "perform your actio ad is showed $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {

                            Text("Show Interstitial on every clicks time lapse off")
                        }


                        Button({
                            adManager.loadAndShowAd(
                                this@MainActivity,
                                "ca-app-pub-3940256099942544/1033173712",
                                clickIntervals = 5,
                                showLoading = true
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "perform your actio ad is showed $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {

                            Text("Show Interstitial on every 5 clicks")
                        }


                        Button({
                            adManager.loadAndShowAd(
                                this@MainActivity,
                                "ca-app-pub-3940256099942544/1033173712",
                                clickIntervals = 5,
                                enableTimeLapse = false,
                                showLoading = true
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "perform your actio ad is showed $it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {

                            Text("Show Interstitial on every 5 clicks timelapse off")
                        }

                        var showAds by remember {
                            mutableStateOf(false)
                        }
                        LaunchedEffect(Unit) {
                            delay(5000)
                            showAds = true
                        }
                        if (showAds) {
                            BannerAd(
                                modifier = Modifier.fillMaxWidth(),
                                adUnit = "ca-app-pub-3940256099942544/6300978111"
                            )

                            BannerAd(
                                modifier = Modifier.fillMaxWidth(),
                                adUnit = "ca-app-pub-3940256099942544/9214589741",
                                true
                            )



                            NativeMedium(
                                Modifier.fillMaxWidth(),
                                "ca-app-pub-3940256099942544/2247696110"
                            )
                            NativeSmall(
                                modifier = Modifier.fillMaxWidth(),
                                "ca-app-pub-3940256099942544/2247696110"
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AdsSdkTheme {
        Greeting("Android")
    }
}