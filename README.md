# AdsSdk

[![Release](https://jitpack.io/v/adnanamin69/Ads-Sdk.svg)](https://jitpack.io/#adnanamin69/Ads-Sdk)

A modular Android SDK for easy integration of AdMob ads (App Open, Interstitial, Banner, and Native)
in your Android applications. Published on JitPack for simple dependency management.

---

## Features

- **App Open Ads**: Show ads when users open or return to your app.
- **Interstitial Ads**: Full-screen ads at natural transition points.
- **Banner Ads**: Adaptive banners for various screen sizes.
- **Native Ads**: Customizable ad layouts for seamless UI integration.
- **GDPR Consent**: Built-in consent management using Google UMP SDK.
- **Sample App**: Included for quick reference and testing.

---

## Installation

### 1. Add JitPack repository

In your root `settings.gradle` or `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency

In your app or module `build.gradle` or `build.gradle.kts`:

```
dependencies {
    implementation("com.github.adnanamin69:AdsSdk:1.0.0") // Use the latest release version from JitPack
}
```

Replace `1.0.0` with the latest version from
the [JitPack Releases](https://jitpack.io/#adnanamin69/AdsSdk).

---

## Usage

### 1. Initialize Consent Manager

```kotlin
val consentManager = GoogleMobileAdsConsentManager(context)
consentManager.gatherConsent(activity) { error ->
    if (consentManager.canRequestAds) {
        MobileAds.initialize(context) {}
        // Ready to load ads
    }
}
```

**Arguments:**

- `context`: The application or activity context.
- `activity`: The current activity instance (required for showing consent dialogs).
- `error`: Callback parameter, returns any error encountered during consent gathering (null if
  successful).

---

### 2. Show App Open Ad

```kotlin
initAppOpenAd("<ad_unit_id>") {
    // Callback after ad is shown
}
```

**Arguments:**

- `<ad_unit_id>`: Your AdMob App Open Ad unit ID (string).
- Callback: Lambda function executed after the ad is shown or dismissed.

---

### 3. Show Interstitial Ad

```kotlin
val adManager = InterstitialAdManager()
adManager.loadAndShowAd(
    context,           // Activity or application context
    "<ad_unit_id>",   // Your AdMob Interstitial Ad unit ID
    clickIntervals = 1, // (Optional) Show ad every Nth click (default: 1)
    showLoading = true, // (Optional) Show loading dialog while ad loads (default: true)
    enableTimeLapse = true, // (Optional) Prevents showing ads too frequently (default: true)
) { wasShown ->
    // Callback after ad is dismissed or not shown
}
```

**Arguments:**

- `context`: The activity or application context.
- `<ad_unit_id>`: Your AdMob Interstitial Ad unit ID (string).
- `clickIntervals`: (Optional) Integer, show ad every Nth click (default: 1).
- `showLoading`: (Optional) Boolean, show a loading dialog while the ad loads (default: true).
- `enableTimeLapse`: (Optional) Boolean, prevents showing ads too frequently (default: true).
- Callback (`wasShown`): Boolean, true if ad was shown, false otherwise.

---

### 4. Load Banner Ad

```kotlin
activity.loadBanner(
    "<ad_unit_id>", // Your AdMob Banner Ad unit ID
    frameLayout     // The FrameLayout where the banner will be displayed
)
```

**Arguments:**

- `<ad_unit_id>`: Your AdMob Banner Ad unit ID (string).
- `frameLayout`: The FrameLayout view in your layout where the banner ad will be loaded.

---

### 5. Load Native Ad

```kotlin
context.nativeAdMainSmall(
    frameAd,        // The FrameLayout where the native ad will be displayed
    "<ad_unit_id>" // Your AdMob Native Ad unit ID
)
```

**Arguments:**

- `frameAd`: The FrameLayout view in your layout where the native ad will be loaded.
- `<ad_unit_id>`: Your AdMob Native Ad unit ID (string).

See the [sample app](app/src/main/java/com/adnan/live/adssdk/MainActivity.kt) for a complete
integration example.

---

## Dependencies

- Google Mobile Ads SDK
- Google UMP SDK (User Messaging Platform)
- AndroidX libraries
- Facebook Shimmer
- sdp-android, ssp-android (for responsive UI)

---

## ProGuard

Consumer and ProGuard rules are provided in the library. If you use ProGuard, make sure to include:

- `AdmobAds/proguard-rules.pro`
- `AdmobAds/consumer-rules.pro`

---

## Contributing

Contributions, issues, and feature requests are welcome! Feel free to open an issue or submit a pull
request.

---

## License

This project is licensed under the [MIT License](LICENSE) and is free for public and commercial use.

---

## Author

Maintained by [Adnan Amin](https://github.com/adnanamin69).

---

## JitPack

For more details, see [JitPack Documentation](https://jitpack.io/docs/).