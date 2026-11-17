# 🟢 Green Player — Modern High-Performance Android Media Player

> **Package Name:** com.greensoft.greenplayer  
> **Target SDK:** Android 14 (API 34) | **Min SDK:** Android 5.0 (API 21)  
> **Engine:** Google Media3 (ExoPlayer 1.2.1) + Glide  
> **Theme:** Obsidian Dark with Neon Green Accents  
> **Compliance:** 100% Google Play Store Ready (Zero Copyrighted / DRM Code)

---

## 🎬 Core Features (MX Player Clone Features)

### 🖐️ Gesture Control System
* **Right Half Vertical Swipe:** Volume level HUD adjustment (0% – 100%)
* **Left Half Vertical Swipe:** Screen brightness HUD adjustment (0% – 100%)
* **Horizontal Swipe (Anywhere):** Fast seek preview with seek difference indicator (+0:15 or -0:30)
* **Double-Tap Controls:**
  * Double-Tap Left: Rewind 10 seconds with toast indicator
  * Double-Tap Right: Fast-Forward 10 seconds with toast indicator
  * Double-Tap Center: Instant Play / Pause toggle
* **Single Tap:** Toggle translucent controller bars with 4-second auto-hide timer

### ⚙️ Video & Playback Controls
* **Aspect Ratio Switching:** Cycle between **Fit to Screen**, **Zoom / Crop**, **Stretch (16:9)**, and **100% (Original)**.
* **Playback Speed Controller:** Smooth speed adjustment from **0.25x** up to **4.0x** with reset button.
* **Audio Track Switcher:** Select from multiple embedded audio languages/streams.
* **Subtitle Engine:** Embedded subtitles (SRT, SSA, ASS, VTT) with track selection and toggle off.
* **Kids Lock (Screen Lock):** Locks the entire screen from accidental touches, showing a subtle floating unlock button.
* **Picture-in-Picture (PiP):** Continue watching videos in floating window while using other apps.
* **Auto-Resume Playback:** Remembers your exact watched position per video file.

### 📂 File & Media Browser
* **Scoped Storage Compliant:** Full support for Android 13/14+ (READ_MEDIA_VIDEO, READ_MEDIA_AUDIO).
* **High-Speed Thumbnails:** Cached with Glide for buttery smooth 60fps scrolling.
* **Tabs:** Videos, Folders, and Audio Music list.
* **Video Properties Dialog:** Inspect file path, resolution, duration, format, and file size.
* **Search & Sort:** Real-time search filter; Sort by Date, Name, Size, or Duration.
* **File Sharing:** Securely share videos to WhatsApp, Telegram, or Google Drive via FileProvider.

---

## 🛠️ How to Open & Build in Android Studio

1. Open **Android Studio**.
2. Select **File -> Open...** and browse to this folder:  
   g:\Sandaruwan\Antigravity\MX Player\GreenPlayer
3. Click **OK** to let Gradle sync dependencies automatically.
4. Connect your Android phone or start an Android Emulator.
5. Click the green **Run (▶)** button to test on your device!

### 📦 To Generate a Signed APK / AAB for Google Play Store:
1. In Android Studio, go to **Build -> Generate Signed Bundle / APK...**
2. Choose **Android App Bundle (.aab)** (Recommended for Play Store) or **APK**.
3. Create or select your upload keystore.
4. Select **Release** build variant.
5. Your production-ready .aab or .apk will be generated in pp/release/!
