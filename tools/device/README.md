# Device & Emulator Guide

This guide describes how to start and interact with the reference emulator, grant permissions, configure test environments (scales, density, locales), restore seeded backups, and capture verified proof screenshots.

---

## 1. Emulator Target

All device work and verification must run strictly on the Android Virtual Device (AVD) **`Violet_API_36`** using device ID `emulator-5554`.

> **Note:** A physical phone may be connected to the workstation. Do **not** interact with any device other than `emulator-5554`.

### Start the Emulator
Start exactly one emulator instance:
```bash
emulator -avd Violet_API_36 -netdelay none -netspeed full &
adb -s emulator-5554 wait-for-device
```

### Terminate the Emulator
Always shut down the emulator after completing verification:
```bash
adb -s emulator-5554 emu kill
```

---

## 2. App Installation & Usage Access

### Clean Install
```bash
adb -s emulator-5554 uninstall io.github.khaledbahaaeldin.emberbyte
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
```

### Grant Usage Access Permission
Emberbyte requires `PACKAGE_USAGE_STATS` to query network statistics:
```bash
adb -s emulator-5554 shell appops set io.github.khaledbahaaeldin.emberbyte GET_USAGE_STATS allow
```

### Restore Usage Access Permission
```bash
adb -s emulator-5554 shell appops set io.github.khaledbahaaeldin.emberbyte GET_USAGE_STATS default
```

---

## 3. Seeded Backup Data

Use `tools/seed/make_backup.py` to create deterministic test backups.

### Generate & Push Backup
```bash
python tools/seed/make_backup.py --out tools/seed/fixtures/small.emberbyte --passphrase test-passphrase --days 7 --scenario basic
adb -s emulator-5554 push tools/seed/fixtures/small.emberbyte /sdcard/Download/small.emberbyte
```

### Restore in App
1. Open Emberbyte on the device.
2. Navigate to **Settings > Backup & Export > Restore Backup**.
3. Select the pushed `.emberbyte` file from Downloads.
4. Enter passphrase: `test-passphrase`.
5. Tap **Restore**. Confirm 1 plan, 1 budget, and 1008 hourly usage rows are loaded.

---

## 4. Environment Settings & Verification

Every setting changed for testing must be restored to default afterwards.

### Font Scale (Accessibility font scale 2.0)
```bash
# Set large font
adb -s emulator-5554 shell settings put system font_scale 2.0

# Restore normal font
adb -s emulator-5554 shell settings put system font_scale 1.0
```

### Screen Density (Display size)
```bash
# Set custom density (e.g. 480 dpi)
adb -s emulator-5554 shell wm density 480

# Restore default density
adb -s emulator-5554 shell wm density reset
```

### Locale & Right-to-Left (RTL) Layout
```bash
# Switch to Arabic (RTL layout)
adb -s emulator-5554 shell setprop persist.sys.locale ar-EG
adb -s emulator-5554 shell setprop debug.force_rtl 1

# Restore English (LTR layout)
adb -s emulator-5554 shell setprop persist.sys.locale en-US
adb -s emulator-5554 shell setprop debug.force_rtl 0
```

### Animation Scales (Reduced Motion)
To verify that animations snap immediately and do not hang when reduced motion is enabled:
```bash
# Disable animations (Reduced Motion)
adb -s emulator-5554 shell settings put global window_animation_scale 0
adb -s emulator-5554 shell settings put global transition_animation_scale 0
adb -s emulator-5554 shell settings put global animator_duration_scale 0

# Restore default animations
adb -s emulator-5554 shell settings put global window_animation_scale 1
adb -s emulator-5554 shell settings put global transition_animation_scale 1
adb -s emulator-5554 shell settings put global animator_duration_scale 1
```

### Night Mode (Dark / Light Theme)
```bash
# Enable Dark Mode
adb -s emulator-5554 shell cmd uimode night yes

# Enable Light Mode
adb -s emulator-5554 shell cmd uimode night no

# Restore Automatic / System Default
adb -s emulator-5554 shell cmd uimode night auto
```

---

## 5. Capturing Proof Screenshots

Statements about UI rendering must be backed by screenshots inspected directly.

```bash
adb -s emulator-5554 exec-out screencap -p > screenshot.png
```
