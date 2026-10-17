# Simple Colorblock Art (SCA)

> A zero-latency, private peer-to-peer physical heartbeat communicator disguised as an abstract home-screen art tile.

<p align="center">
  <img src="art_preview.svg" alt="Simple Colorblock Art Widget Preview" width="600" />
</p>

---
## Acknowledgment

**Simple Colorblock Art** is designed to built quick so we doesnt able to follow best practice please dont blindly trust on codebase. Later, I will audit and makes  this secure with best practice.

## 🎨 Overview & "Ghost App" Architecture

**Simple Colorblock Art** is designed for ultra-private communication between two people without looking like a messenger or notification app:

1. **Zero App Drawer Presence:** There is no launcher icon, no openable screen, and no UI in the app drawer. It exists **strictly as a 2×1 widget** inside Android's native Widgets picker.
2. **The Disguise:** To anyone looking at your phone, it appears to be a high-end designer art widget—a **Modern Expressionist Collage** with floating black concentric rings, minimalist dots, and a hardware monospace coordinate `[ 19 • S ]`.
3. **The Living Core:** The center circle is a dynamic state machine that communicates the entire loop of presence, delivery, and receipt silently and without words.
4. **Zero-Latency Direct Routing:** Bypasses third-party servers by using a serverless Cloudflare Worker executing client-side WebCrypto RS256 JWTs directly against Google's modern Firebase Cloud Messaging (FCM) HTTP v1 API with High-Priority delivery flags.

---

## 🔮 The 3 Living Visual States

| State | Visual Appearance | What It Communicates |
| :--- | :--- | :--- |
| **Idle** | **Solid Pure Black** (`#111111`) inside black rings | All quiet. Both parties are acknowledged. Blends seamlessly with the art tile. |
| **Sent (Waiting)** | **Bright Luminous Green** (`#00E676`) with soft radiant halo | You sent a heartbeat. It is sitting on your partner's home screen waiting to be seen. |
| **Received** | **Bright Electric Red Flame** (`#FF1744`) with radiant halo | Your partner tapped their widget to send you love. It is glowing for you. |

---

## 👆 Touch Controls & Interaction Codes

The widget is powered by a multi-tap burst detection engine with strict timing limits:

* **⚡ 3 Taps within 1.5s (Send Heartbeat):**
  * **Haptic:** Delivers a crisp **220ms confirmation buzz**.
  * **Visual:** Shifts local center core to **Bright Green** (*"Waiting for partner"*).
  * **Network:** Posts an encrypted webhook to Cloudflare, waking the partner's phone instantly.
* **🤝 1 Tap on Red Flame (Acknowledge Heartbeat):**
  * **Haptic:** Delivers a satisfying **180ms solid thud**.
  * **Visual:** Extinguishes the glowing red flame back to **Solid Black**.
  * **Network:** Sends an encrypted `"acknowledge"` packet back to the partner, vibrating their phone and turning their green light back to black.
* **🕵️‍♂️ 7 Rapid Taps (Spy Gadget — Copy FCM Token):**
  * **Haptic:** Delivers a distinct **double-vibration pulse** (`150ms - pause - 150ms`).
  * **Action:** Fetches your phone's unique Firebase Hardware Token and silently copies it to the Android clipboard so you can paste it into Cloudflare without Android Studio or Logcat.
* **🛡️ False-Tap Protection:**
  * Strict **600ms inter-tap threshold**: Pausing for more than 600ms resets the counter immediately. You can never accidentally chain separate 3-tap bursts into a 7-tap action.
  * Accidental single taps while idle do nothing and automatically expire after 600ms.

---

## 📳 Hardware Vibration Engine (Android 15 & 16 Compliant)

Modern Android versions (Android 14, 15, and 16) enforce strict background haptic sandboxing. `VibrationHelper` is engineered to guarantee full physical motor vibration across all modern Android devices:

* **Bypasses Touch-Haptic Mute (`USAGE_NOTIFICATION`):** Tagged with notification communication attributes so Android OS does not mute vibrations even if the user has system keyboard/touch feedback disabled.
* **Hardware Amplitude Control Check:** Dynamically queries `vibrator.hasAmplitudeControl()`. If the hardware supports variable force, it blasts at **amplitude 255 (100% full motor power)**; on binary motors, it uses default amplitude without throwing Android 16's fatal `IllegalArgumentException`.
* **Native LRA Engine Fallback:** Includes a fallback to Android's native hardware haptic engine (`VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)`) for modern linear resonant actuators (Pixel, Samsung Galaxy, OnePlus).

---

## 🏗️ Project Architecture & Flavors

The project uses native Android Product Flavors to compile two distinct APKs:

```
ping/
├── app/
│   ├── src/main/java/.../
│   │   ├── HeartbeatWidget.kt         # Jetpack Glance 2×1 widget UI & state machine
│   │   ├── WidgetClickAction.kt       # Multi-tap burst detector & haptic dispatcher
│   │   ├── HeartbeatFcmService.kt     # High-priority FCM receiver & wakeup engine
│   │   └── VibrationHelper.kt         # Universal Android 15/16 motor driver
│   ├── src/main/res/
│   │   ├── drawable/
│   │   │   ├── art_expressionist_background.xml  # Scalable vector canvas art
│   │   │   ├── concentric_black_rings.xml        # Floating black outline rings
│   │   │   └── transparent_icon.xml              # Transparent vector icon
│   │   ├── layout/initial_layout.xml             # Instant canvas loader
│   │   └── xml/heartbeat_widget_info.xml         # 2×1 AppWidgetProvider metadata
│   └── build.gradle.kts               # R8 minification, flavors & signing config
├── worker.js                          # Serverless Cloudflare routing switchboard
└── local.properties                   # Local secret URLs and tokens
```

* **`sca-him` Flavor:** Built for his phone $\rightarrow$ Automatically targets `"her"`.
* **`sca-her` Flavor:** Built for her phone $\rightarrow$ Automatically targets `"him"`.

---

## 🚀 Complete Step-by-Step Setup Guide

### Step 1: Firebase Project Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project (e.g. `simple-colorblock-art`).
2. Add an **Android App** to the project:
   * **Package Name:** `com.simple.colorblockart` (must match exactly).
3. Download the generated `google-services.json`.

---

### Step 2: Where to Place Google Services Files

1. Place the downloaded `google-services.json` file in the `app/` folder:
   ```
   ping/app/google-services.json
   ```
2. Verify or update `app/src/main/res/values/google_services.xml` with your project numbers from `google-services.json`:
   ```xml
   <resources>
       <string name="google_app_id" translatable="false">YOUR_APP_ID</string>
       <string name="google_api_key" translatable="false">YOUR_API_KEY</string>
       <string name="project_id" translatable="false">YOUR_PROJECT_ID</string>
       <string name="gcm_defaultSenderId" translatable="false">YOUR_PROJECT_NUMBER</string>
   </resources>
   ```

---

### Step 3: Generate Firebase Service Account Key

To allow Cloudflare to authenticate directly with Google's modern FCM HTTP v1 API:
1. In Firebase Console, click the **Gear Icon (Project Settings)** $\rightarrow$ **Service accounts** tab.
2. Click **"Generate new private key"** $\rightarrow$ click **"Generate key"**.
3. A JSON file will download. Open it in a text editor and keep it ready for Step 4.

---

### Step 4: Configure & Deploy Cloudflare Worker

1. Create a free account on [Cloudflare Workers](https://workers.cloudflare.com/).
2. Create a new Worker (e.g. `simple-colorblock-art`).
3. Click **Quick Edit** and paste the contents of [`worker.js`](worker.js).
4. Fill in the top configuration section using the Service Account JSON from Step 3:
   ```javascript
   const APP_SECRET = "Bearer YOUR_SUPER_SECRET_LONG_PASSWORD_HERE";
   const FIREBASE_PROJECT_ID = "your-project-id";
   const FIREBASE_CLIENT_EMAIL = "firebase-adminsdk-xxxxx@your-project-id.iam.gserviceaccount.com";
   const FIREBASE_PRIVATE_KEY = `-----BEGIN PRIVATE KEY-----
   Paste Your Massive Private Key Here
   -----END PRIVATE KEY-----`;

   const TOKENS = {
     "her": "PASTE_HER_FCM_TOKEN_HERE",
     "him": "PASTE_HIS_FCM_TOKEN_HERE"
   };
   ```
5. Click **Save and Deploy**. Copy your Worker's public URL (e.g. `https://simple-colorblock-art.yourname.workers.dev/`).

---

### Step 5: Configure `local.properties`

In your local repository root, create or edit `local.properties`:

```properties
WORKER_URL=https://your-worker-subdomain.workers.dev/
AUTH_TOKEN=Bearer YOUR_SUPER_SECRET_LONG_PASSWORD_HERE
sdk.dir=/path/to/your/Android/Sdk
```

*(Note: `local.properties` is strictly ignored by `.gitignore` and is never committed).*

---

### Step 6: Build the Optimized Release APKs

Run Gradle to compile, minify with R8, and sign both release APKs:

```bash
./gradlew assembleRelease
```

The build will output two lean, 2.5 MB production APKs:
* **His Phone:** `app/build/outputs/apk/him/release/sca-him-release.apk`
* **Her Phone:** `app/build/outputs/apk/her/release/sca-her-release.apk`

---

### Step 7: Install & Pair the Devices

1. Transfer `sca-him-release.apk` to your phone and `sca-her-release.apk` to her phone.
2. Install the APKs. *(Because it is a ghost app, it will not appear in your app drawer).*
3. Long-press your home screen $\rightarrow$ **Widgets** $\rightarrow$ select **Art** $\rightarrow$ place the 2×1 widget on your screen.
4. **Get the Tokens:**
   * On your phone: Tap the widget **7 times fast** $\rightarrow$ feel the double-vibration $\rightarrow$ your FCM token is copied to clipboard! Paste it into Cloudflare under `"him"`.
   * On her phone: Tap the widget **7 times fast** $\rightarrow$ paste her token into Cloudflare under `"her"`.
5. Click **Save and Deploy** in Cloudflare.

**The setup is complete!** Tap your widget 3 times: your widget turns Green, and her phone vibrates and lights up in a vivid Red Flame. Tap her widget once: she sends an acknowledgment back, turning both widgets back to calm Black.
