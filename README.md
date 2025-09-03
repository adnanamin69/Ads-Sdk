# AdsSdk

[![Release](https://jitpack.io/v/adnanamin69/Ads-Sdk.svg)](https://jitpack.io/#adnanamin69/Ads-Sdk)

A modular Android SDK for easy integration of AdMob ads (App Open, Interstitial, Banner, and Native)
in your Android applications. Published on JitPack for simple dependency management.

---

## Features

- **App Open Ads**: Show ads when users open or return to your app.
- **Interstitial Ads**: Full-screen ads at natural transition points.
- **Rewarded Interstitial Ads**: Full-screen ads that grant a reward upon completion.
- **Banner Ads**: Adaptive banners for various screen sizes with collapsible support.
- **Native Ads**: Customizable ad layouts for seamless UI integration (small and medium sizes).
- **Jetpack Compose Support**: Composable functions for easy integration in modern Android apps.
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

## Theming and Color Customization

The SDK ships with a default theme overlay applied to its layouts:

```xml
android:theme="@style/ThemeOverlay.AdsSdk.Default"
```

This overlay maps a set of theme attributes to default color resources, making all ad UI colors dynamic and easily overridable from your app without editing the SDK.

### Available attributes

- `nativeBg`
- `adCardBackgroundColor`
- `adHeadlineTextColor`
- `adBodyTextColor`
- `adBadgeTextColor`
- `adBadgeBackgroundColor`
- `adCtaTextColor`
- `adCtaBackgroundColor`
- `adMediaBackgroundColor`
- `adIconBackgroundColor`

Defaults are provided in the library’s `values/colors.xml` (e.g., `@color/ad_cta_bg`, `@color/ad_headline_text`, etc.).

### Override just one color (recommended)

Because the overlay points to color resources, you can override a single color globally by redefining that color in your app:

```xml
<!-- app/src/main/res/values/colors.xml -->
<resources>
    <!-- Change only CTA background tint -->
    <color name="ad_cta_bg">#FF00AA88</color>
    <!-- Optional: CTA text color -->
    <color name="ad_cta_text">#FFFFFFFF</color>
</resources>
```

No other changes are needed; all unspecified colors keep their defaults.

### Override via app theme (attrs)

If you prefer to set attributes directly in your app theme:

```xml
<style name="AppTheme" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="adCtaBackgroundColor">@color/your_cta_bg</item>
    <item name="adCtaTextColor">@color/your_cta_text</item>
    <!-- Add other items only if you want to override them -->
</style>
```


### Override at runtime (advanced)

```kotlin
// After inflating the ad view
adView.context.theme.applyStyle(R.style.ThemeOverlay_AdsSdk_CtaOnly, true)
```

Note: Redefining the style with the exact same name `ThemeOverlay.AdsSdk.Default` in your app will replace it entirely. Prefer overriding the specific color resource(s) or creating a child overlay as shown above.

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

#### Initialize InterstitialAdManager
Default initialization:
```kotlin
val adManager = InterstitialAdManager()
```

With custom timeout and time-lapse options:
```kotlin
val adManager = InterstitialAdManager(
    adTimeout = 30000L,          // Timeout for loading an ad (ms)
    timeLapseDifference = 15000L // Minimum time between showing ads (ms)
)
```

#### Load and show interstitial
```kotlin
adManager.loadAndShowAd(
    context,            // Activity or application context
    "<ad_unit_id>",    // Your AdMob Interstitial Ad unit ID
    clickIntervals = 1, // (Optional) Show ad every Nth click (default: 1)
    showLoading = true, // (Optional) Show loading dialog while ad loads (default: true)
    enableTimeLapse = true, // (Optional) Prevents showing ads too frequently (default: true)
) { wasShown ->
    // Callback after ad is dismissed or not shown
}
```

#### Rewarded Interstitial

```kotlin
val adManager = InterstitialAdManager()
adManager.loadAndShowAd(
    context,                         // Activity context required to show the ad
    "<ad_unit_id>",                 // Your AdMob Rewarded Interstitial Ad unit ID
    clickIntervals = 1,              // (Optional) Show ad every Nth click (default: 1)
    showLoading = true,              // (Optional) Show loading dialog while ad loads (default: true)
    enableTimeLapse = true,          // (Optional) Prevents showing ads too frequently (default: true)
    isReward = true                  // IMPORTANT: Enable rewarded interstitial
) { wasRewarded ->
    if (wasRewarded) {
        // Grant the reward to the user
    } else {
        // User closed or no reward earned
    }
}
```

**Notes:**
- The callback parameter (`wasRewarded`) is `true` if the user earned the reward, `false` otherwise.
- All configuration options (`adTimeout`, `timeLapseDifference`, `clickIntervals`, `showLoading`, `enableTimeLapse`) work the same as for interstitials.

**Access reward details:**

```kotlin
val adManager = InterstitialAdManager()
adManager.loadAndShowAd(context, "<ad_unit_id>", isReward = true) { wasRewarded ->
    if (wasRewarded) {
        val amount = adManager.rewardAmount
        val type = adManager.rewardType
        // Grant `amount` of `type` to the user
    }
}
```

<!-- (Timeout and time-lapse initialization moved above) -->

**Arguments for loadAndShowAd:**

- `context`: The activity or application context.
- `<ad_unit_id>`: Your AdMob Interstitial Ad unit ID (string).
- `clickIntervals`: (Optional) Integer, show ad every Nth click (default: 1).
- `showLoading`: (Optional) Boolean, show a loading dialog while the ad loads (default: true).
- `enableTimeLapse`: (Optional) Boolean, prevents showing ads too frequently (default: true).
- Callback (`wasShown`): Boolean, true if ad was shown, false otherwise.

---

### 4. Load Banner Ad

#### Traditional View-based Usage
```kotlin
activity.loadBanner(
    "<ad_unit_id>",         // Your AdMob Banner Ad unit ID
    frameLayout,            // The FrameLayout where the banner will be displayed
    adSize = AdSize.BANNER, // Optional: e.g., AdSize.BANNER, LARGE_BANNER, MEDIUM_RECTANGLE
    onFailed = { error ->   // Optional: banner load/show failure
        // Handle error (log, retry, hide placeholder, etc.)
    }
)
```

#### Collapsible Banner Ads
You can also create collapsible banner ads that expand when clicked and collapse when dismissed:

```kotlin
activity.loadBanner(
    "<ad_unit_id>",          // Your AdMob Banner Ad unit ID
    frameLayout,             // The FrameLayout where the banner will be displayed
    collapsible = "top",     // Collapsible position: "top" or "bottom"
    adSize = AdSize.BANNER,  // Optional size
    onFailed = { error ->    // Optional: failed callback
        // Handle error for collapsible banner
    }
)
```

#### Jetpack Compose Usage
```kotlin
BannerAd(
    modifier = Modifier.fillMaxWidth(),
    adUnit = "ca-app-pub-3940256099942544/6300978111",
    onFailed = { error ->
        // Handle banner (compose) failure
    }
)

// With collapsible functionality
BannerAd(
    modifier = Modifier.fillMaxWidth(),
    adUnit = "ca-app-pub-3940256099942544/9214589741",
    collapsible = "top", // or "bottom"
    onFailed = { error ->
        // Handle banner (compose) failure
    }
)
```

**Arguments for Traditional Usage:**

- `<ad_unit_id>`: Your AdMob Banner Ad unit ID (string).
- `frameLayout`: The FrameLayout view in your layout where the banner ad will be loaded.
- `collapsible`: (Optional) String specifying the collapsible position:
  - `"top"`: The top of the expanded ad aligns to the top of the collapsed ad (ad placed at top of screen)
  - `"bottom"`: The bottom of the expanded ad aligns to the bottom of the collapsed ad (ad placed at bottom of screen)
  - `null` or omitted: Regular banner ad (non-collapsible)

**Arguments for Compose Usage:**

- `modifier`: Compose modifier for styling and layout
- `adUnit`: Your AdMob Banner Ad unit ID (string)
- `collapsible`: (Optional) String for collapsible banner ads: "top", "bottom", or null

---

### 5. Load Native Ad

#### Traditional View-based Usage

**Small Native Ad**
```kotlin
context.nativeAdMainSmall(
    frameAd,          // The FrameLayout where the native ad will be displayed
    "<ad_unit_id>",  // Your AdMob Native Ad unit ID
    onFailed = { error ->
        // Handle native small failure
    }
)
```

**Medium Native Ad**
```kotlin
activity.nativeAdMedium(
    frameLayout,     // The FrameLayout where the native ad will be displayed
    "<ad_unit_id>", // Your AdMob Native Ad unit ID
    onFailed = { error ->
        // Handle native medium failure
    }
)
```

#### Jetpack Compose Usage

**Small Native Ad (Text-only)**
```kotlin
NativeSmall(
    modifier = Modifier.fillMaxWidth(),
    unitId = "ca-app-pub-3940256099942544/2247696110",
    onFailed = { error ->
        // Handle native small load/show failure
    }
)
```

**Medium Native Ad (With Media Content)**
```kotlin
NativeMedium(
    modifier = Modifier.fillMaxWidth(),
    unitId = "ca-app-pub-3940256099942544/2247696110",
    onFailed = { error ->
        // Handle native medium load/show failure
    }
)
```

**Arguments for Traditional Usage:**

**Small Native Ad:**
- `frameAd`: The FrameLayout view in your layout where the native ad will be loaded.
- `<ad_unit_id>`: Your AdMob Native Ad unit ID (string).

**Medium Native Ad:**
- `frameLayout`: The FrameLayout view in your layout where the native ad will be loaded.
- `<ad_unit_id>`: Your AdMob Native Ad unit ID (string).

**Arguments for Compose Usage:**

- `modifier`: Compose modifier for styling and layout
- `unitId`: Your AdMob Native Ad unit ID (string)

**Note:** Medium native ads include media content (images/videos) while small native ads are text-only.

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