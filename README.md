# ✂️ TextPop — Android APK Build Guide

## What This App Does
Select any text in **any Android app** → a floating popup appears with:
- **Copy** (green) — copies to clipboard  
- **Google** (blue) — searches Google in browser  
- **MV** (pink) — searches MissAV  

Also auto-detects JAV codes on long press (ported from your userscript).

---

## How to Build the APK

### Step 1 — Install Android Studio
Download from: https://developer.android.com/studio  
(Free, works on Windows/Mac/Linux)

### Step 2 — Open the Project
1. Open Android Studio
2. Click **"Open"** (not New Project)
3. Select the `TextPopApp` folder you downloaded
4. Wait for Gradle sync to finish (~2 minutes first time)

### Step 3 — Build the APK
Go to menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

The APK will be at:
```
TextPopApp/app/build/outputs/apk/debug/app-debug.apk
```

### Step 4 — Install on your phone
- Enable **"Install from unknown sources"** in Android settings
- Transfer the APK to your phone and tap to install
- OR use: `adb install app-debug.apk`

---

## First Launch Setup (2 steps)

1. **Tap "Enable Floating Window Permission"**  
   → Find TextPop in list → toggle ON

2. **Tap "Enable Accessibility Service"**  
   → Find "TextPop Selection" → toggle ON  
   → Accept the warning (it only reads selected text)

✅ Done! Now select any text in any app.

---

## Permissions Explained
| Permission | Why needed |
|---|---|
| Display over other apps | To show the floating popup |
| Accessibility Service | To detect text selection in other apps |
| Internet | To open Google/MV search results |

---

## Project Structure
```
app/src/main/java/com/textpop/app/
├── MainActivity.java                    — Setup screen
├── FloatingWindowService.java           — Floating popup window  
└── TextSelectionAccessibilityService.java — Detects selection in any app

app/src/main/res/
├── layout/floating_menu.xml            — Popup UI (Copy/Google/MV)
├── layout/activity_main.xml            — Setup screen UI
├── drawable/menu_background.xml        — Dark pill background
├── drawable/btn_transparent.xml        — Button press effect
└── xml/accessibility_service_config.xml
```
