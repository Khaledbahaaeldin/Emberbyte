# Galaxy S24+ (One UI 8.5) Live-Update Pill & Now Bar Checklist

> **Note for Auditor / User**: Antigravity cannot access or touch physical devices directly. As mandated by the plan, verification for Samsung Now Bar and the status bar pill is marked as **CANNOT-VERIFY** in automated emulator tests and must be audited on the user's Galaxy S24+ using this checklist.

---

## 1. Prerequisites & Settings Configuration

1. **Install Release or Debug Build**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Open Emberbyte Settings**:
   - Navigate to **Settings** $\to$ **Notifications** section.
   - Verify the **Live notification** switch is enabled (**ON**).
   - Verify the **Live updates** row is present:
     - Subtitle should show `"Allowed"` (or `"Turned off in system settings"` if disabled in Android OS).
     - Secondary subtitle displays: `"Also allow Emberbyte under Now bar in your phone's settings"` (Samsung-specific guidance).
3. **Configure System & One UI Settings**:
   - Tap the **Live updates** row in Emberbyte Settings (this opens the system `MANAGE_APP_PROMOTED_NOTIFICATIONS` screen for Emberbyte).
   - Ensure the toggle **Allow live updates** / **Show as pill in status bar** is enabled.
   - In Samsung One UI Settings:
     - Navigate to: **Settings** $\to$ **Notifications** $\to$ **Now bar** (or **Lock screen and AOD** $\to$ **Now bar widgets**).
     - Ensure **Emberbyte** is enabled in the list of allowed Now bar applications.

---

## 2. Verification Across Plan States

| State | Condition | Expected Chip Text ($\le 7$ chars) | Expected Appearance / Behavior |
|---|---|---|---|
| **Ok** | $< 80\%$ budget used | Today's total (e.g. `1.4 GB`) | Active theme `primary` accent color on status bar pill and progress ring. Now bar displays usage cleanly. |
| **Warning** | $80\% - 99\%$ budget used | Exclamation + remaining (e.g. `!1.2 GB`) | Active theme `warn` accent color on status bar pill and progress ring. |
| **Exceeded** | $\ge 100\%$ or $0\text{ B}$ left | `Over` | Active theme `danger` accent color on status bar pill and progress ring. |

### Verification Steps:
1. Seed an active plan in **Ok** state (or normal daily usage).
2. Check the Galaxy S24+ status bar (near the camera cutout / top area):
   - Confirm a pill appears showing the compact chip text (e.g., `1.4 GB`).
   - Pull down shade slightly: verify the pill smoothly expands into the live update notification without stutter or clipping.
3. Switch or seed a plan into **Warning** state:
   - Status bar pill updates to `!<amount>` (e.g. `!1.2 GB`) with warning tint.
4. Exceed the plan (or seed exhausted plan):
   - Status bar pill displays `Over` with danger tint.
5. Lock the phone to check **Samsung Now bar**:
   - Tap the screen / check Always On Display / Lock screen:
   - Confirm Emberbyte live widget/banner is visible in Now bar with correct state and data.

---

## 3. Daily Dismissal & Reactivation

1. Pull down notification shade while the live update is promoted.
2. Swipe the notification away (dismiss it).
3. Observe:
   - The status bar pill disappears immediately.
   - On the same calendar day, even when network traffic occurs, the live update is **not** promoted back into the status bar or Now bar (it remains unpromoted).
4. Go to Emberbyte **Settings** $\to$ **Notifications**:
   - Toggle **Live notification** **OFF**, then back **ON**.
   - Confirm that clearing the toggle resets the dismissal flag: the live-update pill reappears in the status bar.

---

## 4. Troubleshooting & Diagnostic Commands

If the status bar pill or Samsung Now bar does not appear as expected, execute the following diagnostic commands from your host machine:

### 1. Inspect Notification Dump:
```bash
adb shell dumpsys notification --noredact | grep -A 25 -B 5 "io.github.khaledbahaaeldin.emberbyte"
```
*Look for:*
- `requestPromotedOngoing=true` or `isPromoted=true`
- `shortCriticalText="<chip>"`
- `channelId=live`
- `flags` including `FLAG_ONGOING_EVENT`

### 2. Verify System Promotion Permission:
```bash
adb shell cmd notification can-post-promoted-notifications io.github.khaledbahaaeldin.emberbyte
```
*Expected output:* `true` (if `false`, user must toggle permission in notification settings).

### 3. Check Channel Importance:
```bash
adb shell cmd notification get-channel io.github.khaledbahaaeldin.emberbyte live
```
*Ensure importance is `IMPORTANCE_LOW` (2) or higher (not `IMPORTANCE_MIN`).*

### 4. Monitor Logcat for LiveNotification and Now Bar:
```bash
adb logcat -s "NowBar" "LiveNotification" "NotificationService"
```
