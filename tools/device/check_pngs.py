#!/usr/bin/env python3
import os
import sys

def check_file(path: str) -> bool:
    if not os.path.exists(path):
        print(f"MISSING: {path}")
        return False
    with open(path, "rb") as f:
        header = f.read(4)
    if header != b"\x89PNG":
        print(f"CORRUPT ({header.hex()}): {path}")
        return False
    return True

def main():
    if len(sys.argv) < 2:
        print("Usage: python tools/device/check_pngs.py <dir_or_files...>")
        sys.exit(1)
    
    files = []
    for arg in sys.argv[1:]:
        if os.path.isdir(arg):
            for root, _, fs in os.walk(arg):
                for f in fs:
                    if f.lower().endswith(".png"):
                        files.append(os.path.join(root, f))
        else:
            files.append(arg)
            
    bad = 0
    for f in sorted(files):
        if not check_file(f):
            bad += 1
            
    print(f"\nTotal checked: {len(files)}, Invalid: {bad}")
    if bad > 0:
        sys.exit(1)
    print("ALL PNGs VALID!")

if __name__ == "__main__":
    main()
