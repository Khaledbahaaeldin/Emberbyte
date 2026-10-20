#!/usr/bin/env python3
import os
import subprocess
import sys
import time

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
OUT_DIR = os.path.join(".superpowers", "sdd", "2026-10-17-emberbyte-redesign", "screenshots", "e8")
os.makedirs(OUT_DIR, exist_ok=True)

def run_adb(args):
    cmd = [ADB, "-s", DEVICE] + args
    return subprocess.run(cmd, capture_output=True, text=True)

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

def capture_spec(name, widget, theme="dusk", dark=True, amoled=False, width=260, height=180, perm=True, dormant=False, overcap=False):
    args = [
        "shell", "am", "start", "-W", "-n", "io.github.khaledbahaaeldin.emberbyte/.WidgetPreviewActivity",
        "--es", "widget", widget,
        "--es", "theme", theme,
        "--ez", "dark", str(dark).lower(),
        "--ez", "amoled", str(amoled).lower(),
        "--ei", "width", str(width),
        "--ei", "height", str(height),
        "--ez", "permission", str(perm).lower(),
        "--ez", "dormant", str(dormant).lower(),
        "--ez", "overcap", str(overcap).lower(),
    ]
    run_adb(args)
    time.sleep(1.2)
    out_file = os.path.join(OUT_DIR, f"{name}.png")
    capture_png(out_file)

def main():
    print("Capturing 29 widget test screenshots...")
    # Warm up activity
    run_adb(["shell", "am", "start", "-W", "-n", "io.github.khaledbahaaeldin.emberbyte/.WidgetPreviewActivity",
             "--es", "widget", "today", "--es", "theme", "dusk", "--ez", "dark", "true"])
    time.sleep(2.0)

    # 1. Themes (Dusk dark, Dusk light, Catppuccin light, Gruvbox AMOLED) across widgets
    capture_spec("01_today_dusk_dark_standard", "today", "dusk", True, False, 260, 180)
    capture_spec("02_today_dusk_light_standard", "today", "dusk", False, False, 260, 180)
    capture_spec("03_today_catppuccin_light_standard", "today", "catppuccin", False, False, 260, 180)
    capture_spec("04_today_gruvbox_amoled_standard", "today", "gruvbox", True, True, 260, 180)

    capture_spec("05_plan_dusk_dark_standard", "plan", "dusk", True, False, 260, 180)
    capture_spec("06_plan_dusk_light_standard", "plan", "dusk", False, False, 260, 180)
    capture_spec("07_plan_catppuccin_light_standard", "plan", "catppuccin", False, False, 260, 180)
    capture_spec("08_plan_gruvbox_amoled_standard", "plan", "gruvbox", True, True, 260, 180)

    capture_spec("09_bento_dusk_dark_3x2", "bento", "dusk", True, False, 280, 180)
    capture_spec("10_bento_dusk_light_3x2", "bento", "dusk", False, False, 280, 180)
    capture_spec("11_bento_catppuccin_light_3x2", "bento", "catppuccin", False, False, 280, 180)
    capture_spec("12_bento_gruvbox_amoled_3x2", "bento", "gruvbox", True, True, 280, 180)

    capture_spec("13_dashboard_dusk_dark_4x4", "dashboard", "dusk", True, False, 300, 300)
    capture_spec("14_dashboard_dusk_light_4x4", "dashboard", "dusk", False, False, 300, 300)
    capture_spec("15_dashboard_catppuccin_light_4x4", "dashboard", "catppuccin", False, False, 300, 300)
    capture_spec("16_dashboard_gruvbox_amoled_4x4", "dashboard", "gruvbox", True, True, 300, 300)

    # 2. Resizing between smallest and largest sizes
    capture_spec("17_today_compact_1x1", "today", "dusk", True, False, 140, 100)
    capture_spec("18_today_wide_4x2", "today", "dusk", True, False, 320, 160)
    capture_spec("19_plan_compact_2x1", "plan", "dusk", True, False, 160, 100)
    capture_spec("20_bento_compact_2x2", "bento", "dusk", True, False, 180, 160)
    capture_spec("21_dashboard_compact_4x2", "dashboard", "dusk", True, False, 300, 160)

    # 3. Special states: permission missing, dormant, overcap, hero
    capture_spec("22_today_permission_missing", "today", "dusk", True, False, 260, 180, perm=False)
    capture_spec("23_plan_dormant_state", "plan", "dusk", True, False, 260, 180, dormant=True)
    capture_spec("24_plan_overcap_state", "plan", "dusk", True, False, 260, 180, overcap=True)
    capture_spec("25_hero_compatibility_placed", "hero", "dusk", True, False, 260, 180)

    # 4. Font scale 2.0
    run_adb(["shell", "settings", "put", "system", "font_scale", "2.0"])
    capture_spec("26_today_font_scale_2", "today", "dusk", True, False, 260, 200)
    capture_spec("27_dashboard_font_scale_2", "dashboard", "dusk", True, False, 300, 340)
    run_adb(["shell", "settings", "put", "system", "font_scale", "1.0"])

    # 5. RTL
    run_adb(["shell", "settings", "put", "global", "debug.force_rtl", "1"])
    run_adb(["shell", "am", "kill", "io.github.khaledbahaaeldin.emberbyte"])
    capture_spec("28_today_rtl", "today", "dusk", True, False, 260, 180)
    capture_spec("29_bento_rtl", "bento", "dusk", True, False, 280, 180)
    run_adb(["shell", "settings", "put", "global", "debug.force_rtl", "0"])
    run_adb(["shell", "am", "kill", "io.github.khaledbahaaeldin.emberbyte"])

    print("All captures completed successfully!")

if __name__ == "__main__":
    main()
