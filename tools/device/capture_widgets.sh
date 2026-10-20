#!/usr/bin/env bash
set -euo pipefail

# Find adb if not in PATH
if command -v adb >/dev/null 2>&1; then
    ADB="adb"
elif [ -n "${LOCALAPPDATA:-}" ] && [ -x "$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" ]; then
    ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
elif [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
    ADB="$ANDROID_HOME/platform-tools/adb"
elif [ -x "$HOME/AppData/Local/Android/Sdk/platform-tools/adb.exe" ]; then
    ADB="$HOME/AppData/Local/Android/Sdk/platform-tools/adb.exe"
else
    echo "ERROR: adb not found" >&2
    exit 1
fi

DEVICE="${ADB_DEVICE:-emulator-5554}"
OUT_DIR="${1:-.superpowers/sdd/2026-10-17-emberbyte-redesign/screenshots/e8}"
mkdir -p "$OUT_DIR"

echo "Capturing widget verification screenshots to $OUT_DIR using $DEVICE..."

capture() {
    local name="$1"
    local widget="$2"
    local theme="$3"
    local dark="$4"
    local amoled="$5"
    local width="$6"
    local height="$7"
    local perm="${8:-true}"
    local dormant="${9:-false}"
    local overcap="${10:-false}"

    "$ADB" -s "$DEVICE" shell am start -W -n io.github.khaledbahaaeldin.emberbyte/.WidgetPreviewActivity \
        --es widget "$widget" \
        --es theme "$theme" \
        --ez dark "$dark" \
        --ez amoled "$amoled" \
        --ei width "$width" \
        --ei height "$height" \
        --ez permission "$perm" \
        --ez dormant "$dormant" \
        --ez overcap "$overcap" >/dev/null
    sleep 1.2
    python tools/device/capture_png.py "$OUT_DIR/$name.png" "$DEVICE"
}

# 1. Themes (Dusk dark, Dusk light, Catppuccin light, Gruvbox AMOLED) across widgets
capture "01_today_dusk_dark_standard" "today" "dusk" true false 260 180
capture "02_today_dusk_light_standard" "today" "dusk" false false 260 180
capture "03_today_catppuccin_light_standard" "today" "catppuccin" false false 260 180
capture "04_today_gruvbox_amoled_standard" "today" "gruvbox" true true 260 180

capture "05_plan_dusk_dark_standard" "plan" "dusk" true false 260 180
capture "06_plan_dusk_light_standard" "plan" "dusk" false false 260 180
capture "07_plan_catppuccin_light_standard" "plan" "catppuccin" false false 260 180
capture "08_plan_gruvbox_amoled_standard" "plan" "gruvbox" true true 260 180

capture "09_bento_dusk_dark_3x2" "bento" "dusk" true false 280 180
capture "10_bento_dusk_light_3x2" "bento" "dusk" false false 280 180
capture "11_bento_catppuccin_light_3x2" "bento" "catppuccin" false false 280 180
capture "12_bento_gruvbox_amoled_3x2" "bento" "gruvbox" true true 280 180

capture "13_dashboard_dusk_dark_4x4" "dashboard" "dusk" true false 300 300
capture "14_dashboard_dusk_light_4x4" "dashboard" "dusk" false false 300 300
capture "15_dashboard_catppuccin_light_4x4" "dashboard" "catppuccin" false false 300 300
capture "16_dashboard_gruvbox_amoled_4x4" "dashboard" "gruvbox" true true 300 300

# 2. Resizing between smallest and largest sizes
capture "17_today_compact_1x1" "today" "dusk" true false 140 100
capture "18_today_wide_4x2" "today" "dusk" true false 320 160
capture "19_plan_compact_2x1" "plan" "dusk" true false 160 100
capture "20_bento_compact_2x2" "bento" "dusk" true false 180 160
capture "21_dashboard_compact_4x2" "dashboard" "dusk" true false 300 160

# 3. Special states: permission missing, dormant, overcap
capture "22_today_permission_missing" "today" "dusk" true false 260 180 false false false
capture "23_plan_dormant_state" "plan" "dusk" true false 260 180 true true false
capture "24_plan_overcap_state" "plan" "dusk" true false 260 180 true false true
capture "25_hero_compatibility_placed" "hero" "dusk" true false 260 180

# 4. Font scale 2.0 and RTL
"$ADB" -s "$DEVICE" shell settings put system font_scale 2.0
capture "26_today_font_scale_2" "today" "dusk" true false 260 200
capture "27_dashboard_font_scale_2" "dashboard" "dusk" true false 300 340
"$ADB" -s "$DEVICE" shell settings put system font_scale 1.0

"$ADB" -s "$DEVICE" shell settings put global debug.force_rtl 1
"$ADB" -s "$DEVICE" shell am kill io.github.khaledbahaaeldin.emberbyte
capture "28_today_rtl" "today" "dusk" true false 260 180
capture "29_bento_rtl" "bento" "dusk" true false 280 180
"$ADB" -s "$DEVICE" shell settings put global debug.force_rtl 0
"$ADB" -s "$DEVICE" shell am kill io.github.khaledbahaaeldin.emberbyte

echo "All widget captures completed successfully."
