#!/usr/bin/env python3
import os
import subprocess
import sys

def get_adb():
    local_app_data = os.environ.get("LOCALAPPDATA", "")
    adb_candidates = [
        "adb",
        os.path.join(local_app_data, "Android", "Sdk", "platform-tools", "adb.exe"),
    ]
    for c in adb_candidates:
        if os.path.exists(c):
            return c
    return "adb"

def capture_png(out_path: str, device: str = "emulator-5554"):
    adb = get_adb()
    os.makedirs(os.path.dirname(os.path.abspath(out_path)), exist_ok=True)
    with open(out_path, "wb") as f:
        res = subprocess.run([adb, "-s", device, "exec-out", "screencap", "-p"], stdout=f)
        if res.returncode != 0:
            raise RuntimeError(f"adb screencap failed with exit code {res.returncode}")
    
    with open(out_path, "rb") as f:
        header = f.read(8)
    if not header.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ValueError(f"Corrupted PNG header in {out_path}: {header[:4].hex()}")
    print(f"Captured valid PNG: {out_path} ({os.path.getsize(out_path)} bytes)")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python tools/device/capture_png.py <output_path> [device]")
        sys.exit(1)
    target = sys.argv[1]
    dev = sys.argv[2] if len(sys.argv) > 2 else "emulator-5554"
    capture_png(target, dev)
