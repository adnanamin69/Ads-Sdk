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
    implementation("com.github.adnanamin69:AdsSdk:1.2") // Use the latest release version from JitPack
}
```

Replace `1.2` with the latest version from
the [JitPack Releases](https://jitpack.io/#adnanamin69/Ads-Sdk).

---

## ⚠️ Mandatory Theme Configuration

You **must** add the following items to your app-level theme (typically in `app/src/main/res/values/themes.xml`):

```xml
<style name="AppTheme" parent="android:Theme.Material.Light.NoActionBar">
  <item name="colorPrimary">#F44336</item>
  <item name="backgroundColor">#7E8539</item>
  <item name="nativeBg">#903B3B</item>
</style>
```

If you use a different theme name, add these `<item>` elements to your main app theme.

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

### 2. Show App Open Ad (Immediate)

```kotlin
initAppOpenAd("<ad_unit_id>") {
    // Callback after ad is shown
}
```

**Note:**
- This function is used to **load and show an App Open ad immediately** (e.g., in a splash screen or at any specific point in your app). It does **not** automatically handle showing ads on app resume.
- Use this if you want to control exactly when the App Open ad appears.

**Arguments:**

- `<ad_unit_id>`: Your AdMob App Open Ad unit ID (string).
- Callback: Lambda function executed after the ad is shown or dismissed.

#### For Automatic App Open Ads on Resume
To automatically show App Open ads when your app resumes (recommended for best user experience), use the `AppOpenAdManager` as shown below (see `MyApp.kt`):

```kotlin
class MyApp : Application() {
    val appOpenManger: AppOpenAdManager by lazy {
        AppOpenAdManager(this, "<ad_unit_id>")
    }

    override fun onCreate() {
        super.onCreate()
        appOpenManger.forceLoadAd()
    }
}
```

---

### 3. Show Interstitial Ad

#### Basic Usage
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

#### Custom Timeout and Time Lapse
You can now customize the ad loading timeout and the minimum time between ad displays by passing arguments to the `InterstitialAdManager` constructor:

```kotlin
val adManager = InterstitialAdManager(
    adTimeout = 30000L,         // Timeout for loading an ad in milliseconds (default: 30000L = 30s)
    timeLapseDifference = 15000L // Minimum time in milliseconds between showing ads (default: 15000L = 15s)
)
```

- `adTimeout`: How long to wait for an ad to load before timing out (in milliseconds).
- `timeLapseDifference`: The minimum time (in milliseconds) that must pass before another ad can be shown.

You can then use `adManager.loadAndShowAd(...)` as shown above.

**Arguments for loadAndShowAd:**

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