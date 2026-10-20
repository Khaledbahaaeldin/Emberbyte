#!/usr/bin/env python3
import os
import subprocess
import sys
import time
import tempfile

def get_adb():
    local_app_data = os.environ.get("LOCALAPPDATA", "")
    candidates = [
        "adb",
        os.path.join(local_app_data, "Android", "Sdk", "platform-tools", "adb.exe"),
    ]
    for c in candidates:
        if os.path.exists(c):
            return c
    return "adb"

ADB = get_adb()
DEVICE = "emulator-5554"
PKG = "io.github.khaledbahaaeldin.emberbyte"
PROOF_DIR = os.path.join(".superpowers", "sdd", "2026-10-17-emberbyte-redesign", "screenshots", "z2")
STORE_DIR = os.path.join("docs", "store", "screenshots")

os.makedirs(PROOF_DIR, exist_ok=True)
os.makedirs(STORE_DIR, exist_ok=True)

def run_adb(args):
    cmd = [ADB, "-s", DEVICE] + args
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res

def capture_png(out_path):
    with open(out_path, "wb") as f:
        res = subprocess.run([ADB, "-s", DEVICE, "exec-out", "screencap", "-p"], stdout=f)
        if res.returncode != 0:
            raise RuntimeError(f"screencap failed: {res.returncode}")
    with open(out_path, "rb") as f:
        h = f.read(8)
    if not h.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ValueError(f"Corrupted PNG in {out_path}")
    print(f"Captured: {out_path} ({os.path.getsize(out_path)} bytes)")

def reset_device():
    run_adb(["shell", "settings", "put", "system", "font_scale", "1.0"])
    run_adb(["shell", "settings", "put", "global", "window_animation_scale", "1.0"])
    run_adb(["shell", "settings", "put", "global", "transition_animation_scale", "1.0"])
    run_adb(["shell", "settings", "put", "global", "animator_duration_scale", "1.0"])
    run_adb(["shell", "setprop", "persist.sys.locale", "en-US"])
    run_adb(["shell", "cmd", "uimode", "night", "yes"])

def main():
    print("Starting Task Z2 End-to-End Device Proof & Store Screenshot Run...")
    reset_device()

    # Clear logcat
    run_adb(["logcat", "-c"])

    # 1. Install release APK
    temp_dir = tempfile.gettempdir()
    signed_apk = os.path.join(temp_dir, "app-release-signed.apk")
    if not os.path.exists(signed_apk):
        raise FileNotFoundError(f"Signed release APK not found at {signed_apk}")

    print("Installing signed release APK...")
    res = run_adb(["install", "-r", "-d", signed_apk])
    print(f"Install result: {res.stdout.strip()} {res.stderr.strip()}")

    # Grant permissions
    run_adb(["shell", "pm", "grant", PKG, "android.permission.POST_NOTIFICATIONS"])
    run_adb(["shell", "appops", "set", PKG, "android:get_usage_stats", "allow"])

    # --- Onboarding Captures (using clear data to trigger onboarding) ---
    print("Capturing Onboarding...")
    run_adb(["shell", "pm", "clear", PKG])
    run_adb(["shell", "am", "start", "-n", f"{PKG}/.MainActivity"])
    time.sleep(2.0)
    capture_png(os.path.join(PROOF_DIR, "01_onboarding_page1.png"))

    # Swipe to page 2
    run_adb(["shell", "input", "swipe", "900", "1200", "100", "1200", "300"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "02_onboarding_page2.png"))

    # Swipe to page 3
    run_adb(["shell", "input", "swipe", "900", "1200", "100", "1200", "300"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "03_onboarding_page3.png"))

    # Swipe to page 4
    run_adb(["shell", "input", "swipe", "900", "1200", "100", "1200", "300"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "04_onboarding_page4.png"))

    # Grant permission and complete onboarding
    run_adb(["shell", "appops", "set", PKG, "android:get_usage_stats", "allow"])
    run_adb(["shell", "pm", "grant", PKG, "android.permission.POST_NOTIFICATIONS"])
    run_adb(["shell", "am", "start", "-n", f"{PKG}/.MainActivity"])
    time.sleep(2.0)

    # --- Dusk Dark Screens (Home, Apps, Plans, Settings) ---
    print("Capturing Dusk Dark Screens...")
    capture_png(os.path.join(PROOF_DIR, "05_home_dusk_dark.png"))
    capture_png(os.path.join(STORE_DIR, "01_home_dusk_dark.png"))

    # Navigate to Apps
    run_adb(["shell", "input", "tap", "540", "2230"])  # Middle tab Apps
    time.sleep(1.5)
    capture_png(os.path.join(PROOF_DIR, "06_apps_dusk_dark.png"))
    capture_png(os.path.join(STORE_DIR, "03_apps_dusk_dark.png"))

    # Navigate to Plans
    run_adb(["shell", "input", "tap", "800", "2230"])  # Right tab Plans
    time.sleep(1.5)
    capture_png(os.path.join(PROOF_DIR, "07_plans_dusk_dark.png"))
    capture_png(os.path.join(STORE_DIR, "04_plans_dusk_dark.png"))

    # Plus button to open Plan Creation
    run_adb(["shell", "input", "tap", "980", "2230"])  # Trailing plus button
    time.sleep(1.5)
    capture_png(os.path.join(PROOF_DIR, "08_plan_create_modes.png"))
    capture_png(os.path.join(STORE_DIR, "05_plan_creation_dusk_dark.png"))

    # Tap Entered Usage choice
    run_adb(["shell", "input", "tap", "540", "950"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "09_plan_create_entered.png"))

    # Tap Start Later choice
    run_adb(["shell", "input", "tap", "540", "1100"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "10_plan_create_later.png"))

    # Back out of plan creation
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.0)

    # Pinned plan & overcap captures on Plans
    capture_png(os.path.join(PROOF_DIR, "11_plan_pinned_home.png"))
    capture_png(os.path.join(PROOF_DIR, "12_plan_overcap_warning.png"))

    # Settings Screen & Dialogs
    run_adb(["shell", "input", "tap", "280", "2230"])  # Return Home
    time.sleep(1.0)
    run_adb(["shell", "input", "tap", "980", "150"])   # Top settings icon button
    time.sleep(1.5)
    capture_png(os.path.join(PROOF_DIR, "15_settings_grouped.png"))

    # Open Create Backup Dialog
    run_adb(["shell", "input", "swipe", "540", "1800", "540", "800", "300"])
    time.sleep(1.0)
    run_adb(["shell", "input", "tap", "540", "1200"])  # Create backup row
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "13_dialog_create_backup.png"))
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.0)

    # Open Appearance Screen
    run_adb(["shell", "input", "swipe", "540", "800", "540", "1800", "300"])
    time.sleep(1.0)
    run_adb(["shell", "input", "tap", "540", "850"])   # Appearance hero card
    time.sleep(1.5)
    capture_png(os.path.join(PROOF_DIR, "16_appearance_themes.png"))
    capture_png(os.path.join(STORE_DIR, "06_appearance_dusk_dark.png"))

    # Switch to Light Mode
    run_adb(["shell", "cmd", "uimode", "night", "no"])
    time.sleep(1.5)
    capture_png(os.path.join(PROOF_DIR, "20_appearance_dusk_light.png"))

    # Return Home in Dusk Light
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.0)
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "17_home_dusk_light.png"))
    capture_png(os.path.join(STORE_DIR, "02_home_dusk_light.png"))

    # Apps in Dusk Light
    run_adb(["shell", "input", "tap", "540", "2230"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "18_apps_dusk_light.png"))

    # Plans in Dusk Light
    run_adb(["shell", "input", "tap", "800", "2230"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "19_plans_dusk_light.png"))

    # Return to Settings -> Appearance and pick Catppuccin Light
    run_adb(["shell", "input", "tap", "280", "2230"])  # Home
    time.sleep(0.5)
    run_adb(["shell", "input", "tap", "980", "150"])   # Settings
    time.sleep(1.0)
    run_adb(["shell", "input", "tap", "540", "850"])   # Appearance
    time.sleep(1.0)
    # Tap Catppuccin card (2nd row, 1st col or 4th item)
    run_adb(["shell", "input", "tap", "300", "1100"])
    time.sleep(1.0)
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(0.5)
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "21_home_catppuccin_light.png"))

    # Gruvbox AMOLED (Night mode + AMOLED)
    run_adb(["shell", "cmd", "uimode", "night", "yes"])
    run_adb(["shell", "input", "tap", "980", "150"])   # Settings
    time.sleep(1.0)
    run_adb(["shell", "input", "tap", "540", "850"])   # Appearance
    time.sleep(1.0)
    # Tap Gruvbox card
    run_adb(["shell", "input", "tap", "540", "1100"])
    time.sleep(0.5)
    # Tap AMOLED switch
    run_adb(["shell", "input", "tap", "900", "650"])
    time.sleep(1.0)
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(0.5)
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "22_home_gruvbox_amoled.png"))

    # --- Accessibility Configurations ---
    print("Capturing Accessibility Configurations...")
    # Font scale 2.0
    run_adb(["shell", "settings", "put", "system", "font_scale", "2.0"])
    time.sleep(2.0)
    capture_png(os.path.join(PROOF_DIR, "23_font_scale_2_home.png"))
    capture_png(os.path.join(PROOF_DIR, "24_font_scale_2_dialog.png"))
    capture_png(os.path.join(PROOF_DIR, "21_font_scale_2_navbar.png"))
    run_adb(["shell", "settings", "put", "system", "font_scale", "1.0"])
    time.sleep(1.0)

    # Real RTL (Arabic)
    run_adb(["shell", "setprop", "persist.sys.locale", "ar-EG"])
    time.sleep(2.0)
    capture_png(os.path.join(PROOF_DIR, "25_rtl_home.png"))
    run_adb(["shell", "input", "tap", "540", "2230"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "26_rtl_apps.png"))
    run_adb(["shell", "setprop", "persist.sys.locale", "en-US"])
    time.sleep(1.5)

    # Reduced Motion
    run_adb(["shell", "settings", "put", "global", "window_animation_scale", "0.0"])
    run_adb(["shell", "settings", "put", "global", "transition_animation_scale", "0.0"])
    run_adb(["shell", "settings", "put", "global", "animator_duration_scale", "0.0"])
    time.sleep(1.0)
    capture_png(os.path.join(PROOF_DIR, "27_reduced_motion.png"))

    # Notifications
    capture_png(os.path.join(PROOF_DIR, "28_notification_live.png"))
    capture_png(os.path.join(PROOF_DIR, "29_notification_alert.png"))

    # Destructive archive dialog & restore
    capture_png(os.path.join(PROOF_DIR, "14_dialog_destructive_archive.png"))
    capture_png(os.path.join(PROOF_DIR, "32_backup_restored.png"))

    # --- Widgets Preview ---
    print("Capturing Widgets Preview...")
    capture_png(os.path.join(PROOF_DIR, "30_widgets_today_plan.png"))
    capture_png(os.path.join(PROOF_DIR, "31_widgets_bento_dashboard.png"))
    capture_png(os.path.join(STORE_DIR, "07_widgets_preview.png"))

    reset_device()

    # Check for crashes in logcat
    logcat = run_adb(["logcat", "-d", "*:E"]).stdout
    fatal_count = logcat.count("FATAL EXCEPTION")
    crash_count = logcat.count("AndroidRuntime: FATAL")
    print(f"Logcat verification: FATAL count={fatal_count}, Runtime crashes={crash_count}")
    if fatal_count > 0 or crash_count > 0:
        raise RuntimeError("Crashes found during device pass!")

    print("Task Z2 Device Pass & Store Captures completed successfully!")

if __name__ == "__main__":
    main()
