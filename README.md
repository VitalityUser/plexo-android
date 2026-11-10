# Plexo Android ⚡

> **Multi-Link Aggregation Downloader for Android**  
> *UI Design & Art Direction by [@vitalityuser](https://github.com/vitalityuser)*

An open-source Android download manager that combines **Wi-Fi + Mobile Cellular (5G/4G)** into a single bonded high-speed pipeline. Features automatic download sniffing from Chrome and a built-in browser, wrapped in a frosted glass sage aesthetic.

---

## 🚀 Key Features

- **⚡ Simultaneous Wi-Fi & Cellular Bonding**:
  Uses Android's `ConnectivityManager.requestNetwork` to force both Wi-Fi and Mobile Data to stay active simultaneously, routing chunks through per-socket `OkHttpClient` bindings.
- **🎯 Auto-Detect Downloads from Chrome & Any Browser**:
  Share any link to Plexo or tap direct download links (`.apk`, `.zip`, `.rar`, `.7z`, `.iso`, `.mp4`, `.bin`, `.exe`, etc.) to instantly begin bonded downloading.
- **📋 Smart Clipboard Sniffer**:
  Automatically detects copied file links from your clipboard when switching back to Plexo.
- **🌐 Dedicated In-App Plexo Browser**:
  Built-in browser with 1-tap download interception (`DownloadListener`), quick bookmark shortcuts, and automatic switching to the real-time speedometers.
- **🎨 Frosted Sage Glassmorphism UI**:
  Signature translucent glass cards, radial speed dials, live canvas telemetry waveforms, and a floating dark pill bottom navigation bar.
- **🛡️ Background Resiliency**:
  Runs via an Android `ForegroundService` with WakeLock protection so multi-stream downloads continue at maximum speed even when your screen is locked.

---

## ⚡ Quick Setup Guide (How to Bond Networks)

1. **Enable Mobile Data Always Active**:
   - Go to Android **Settings > Developer Options**.
   - Turn **ON** **"Mobile data always active"** (allows Android to keep Cellular alive even while connected to Wi-Fi).
2. **Turn On Both Connections**:
   - Enable both **Wi-Fi** and **Mobile Data**. The top badge in Plexo will illuminate **`⚡ BONDED`**.
3. **Download from Chrome or Built-in Browser**:
   - In Chrome, tap **Share > Plexo**, copy any download URL, or browse websites in Plexo's **Browser** tab.
4. **Set Battery to Unrestricted**:
   - In App Info > Battery, select **Unrestricted** to prevent the OS from pausing background streams.

---

## 🛠️ Build & Install

### Building the APK
```powershell
# Clone the repository
git clone https://github.com/vitalityuser/plexo-android.git
cd plexo-android

# Build Debug APK
./gradlew assembleDebug
```
The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

### Installing via ADB
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 👤 Author & Design
- **Author & Designer**: [@vitalityuser](https://github.com/vitalityuser)
- **License**: MIT
