# CleanBar - Shizuku Status Bar Hider for Android 📱✨

CleanBar allows you to **hide the status bar completely** or **clean it (Demo Mode)** across all apps on your Android phone using Shizuku (ADB privileged service) without root. Perfect for clean, distraction-free screenshots!

---

## 🚀 How It Works
1. **Shizuku API Binding**: Communicates directly with the Shizuku Binder service to gain ADB-level privileges.
2. **Policy Control**: Uses Android's `policy_control` system setting to dynamically toggle immersive status bar mode (`immersive.status=*`).
3. **Demo Mode (SystemUI)**: For modern Android versions (11-15+), broadcasts SystemUI demo commands to fix the time to 12:00, battery to 100%, and hide all notification clutter.
4. **Quick Settings Tile**: Add a tile to your notification panel to hide/show the status bar with a single tap before taking screenshots!

---

## 🛠️ Step-by-Step Setup Guide

### Step 1: Install Shizuku
1. Download & Install **Shizuku** from Google Play Store or GitHub.
2. Open Shizuku and start the service:
   - **Method A (Wireless Debugging - No PC needed, Android 11+)**:
     - Go to Developer Options -> Enable **Wireless Debugging**.
     - Open Shizuku -> Tap "Pairing" -> Enter the 6-digit code.
     - Tap "Start" in Shizuku.
   - **Method B (Via PC/ADB, Android 8 - 10)**:
     - Connect phone to PC with USB Debugging enabled.
     - Run: `adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh`

### Step 2: Install CleanBar App
1. Open this project in **Android Studio** and build the APK (`Build -> Build APK(s)`).
2. Install the generated APK on your phone.
3. Open CleanBar and tap **"Authorize"** when prompted to grant Shizuku permission.

### Step 3: Use the Quick Settings Tile (Recommended)
1. Swipe down your Android notification panel twice.
2. Tap the **Edit / Pencil icon (✏️)**.
3. Find **"CleanBar Hider"** and drag it into your active tiles.
4. Now whenever you want to take a clean screenshot:
   - Tap the tile -> Status bar hides immediately!
   - Take your screenshot (Power + Volume Down).
   - Tap the tile again -> Status bar is restored!

---

## 💻 Technical Details
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35)
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose + Material 3
- **Privilege Provider**: `dev.rikka.shizuku:api:13.1.5`
