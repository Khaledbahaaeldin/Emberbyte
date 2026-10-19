#!/usr/bin/env python3
"""
gen-licences.py: Generates licences.json from docs/release/dependency-licences.txt,
NOTICE, and licenses/ directory.

Output file: app/src/main/assets/licences.json
Format per entry:
{
    "name": str,
    "version": str,
    "licence": str,
    "url": str,
    "category": str,
    "fullLicenceText": str
}
"""

import json
import os
import re
import sys
from pathlib import Path

def find_repo_root() -> Path:
    candidates = [
        Path("."),
        Path(".."),
        Path(__file__).resolve().parent.parent
    ]
    for c in candidates:
        if (c / "docs" / "release" / "dependency-licences.txt").exists():
            return c.resolve()
    raise FileNotFoundError("Could not locate repository root")

def main():
    root = find_repo_root()
    dep_file = root / "docs" / "release" / "dependency-licences.txt"
    notice_file = root / "NOTICE"
    licenses_dir = root / "licenses"
    output_file = root / "app" / "src" / "main" / "assets" / "licences.json"

    if not dep_file.exists():
        sys.exit(f"Missing {dep_file}")
    if not notice_file.exists():
        sys.exit(f"Missing {notice_file}")

    notice_text = notice_file.read_text(encoding="utf-8")
    tp_text = notice_text.split("Third-party material\n--------------------")[1].strip()
    raw_notice_entries = re.split(r"\n\n(?=[^\s])", tp_text)

    # Pre-parse palettes from NOTICE
    palette_entry_raw = next(r for r in raw_notice_entries if "Community colour palettes" in r)
    raw_palettes = re.split(r"\n  (?=[A-Za-z])", palette_entry_raw)[1:]
    palette_info = {}
    for p in raw_palettes:
        p_lines = [l.strip() for l in p.strip().splitlines()]
        p_name = p_lines[0]
        p_cr = p_lines[1] if len(p_lines) > 1 else ""
        p_url = p_lines[2] if len(p_lines) > 2 else ""
        palette_info[p_name] = (p_cr, p_url)

    # Standard Apache 2.0 text template or read from NOTICE
    apache_2_notice = "Apache License 2.0\nLicensed under the Apache License, Version 2.0 (the \"License\");\nyou may not use this file except in compliance with the License.\nYou may obtain a copy of the License at\nhttp://www.apache.org/licenses/LICENSE-2.0"

    # Token matchers for dependency lines to find their NOTICE section
    dep_tokens = {
        "okio": ["com.squareup.okio:okio", "okio"],
        "kotlinx-serialization": ["kotlinx-serialization", "kotlinx.serialization"],
        "guava listenablefuture": ["listenablefuture"],
        "jspecify": ["org.jspecify:jspecify"],
        "JetBrains Compose Multiplatform": ["JetBrains Compose", "org.jetbrains.compose"],
        "shaded protobuf": ["shaded inside AndroidX DataStore"],
        "Bouncy Castle": ["Bouncy Castle"],
        "Roboto Flex": ["Roboto Flex"],
        "Jetpack Compose": ["AndroidX Jetpack Compose and Material 3"],
        "Material 3": ["AndroidX Jetpack Compose and Material 3"],
        "AndroidX Room": ["AndroidX Room"],
        "AndroidX WorkManager": ["AndroidX WorkManager"],
        "AndroidX Glance": ["AndroidX Glance\n"],
        "AndroidX DataStore": ["AndroidX DataStore\n"],
        "AndroidX Core": ["AndroidX Core and SavedState"],
        "AndroidX Lifecycle": ["AndroidX Lifecycle"],
        "AndroidX Activity": ["AndroidX Activity"],
        "AndroidX Navigation": ["AndroidX Navigation\n"],
        "AndroidX SQLite": ["AndroidX SQLite"],
        "AndroidX Startup": ["AndroidX Startup"],
        "AndroidX Tracing": ["AndroidX Tracing"],
        "AndroidX Window": ["AndroidX Window"],
        "AndroidX Graphics": ["AndroidX Graphics"],
        "Kotlin": ["kotlin-stdlib"],
        "kotlinx-coroutines": ["kotlinx-coroutines"],
        "JetBrains Annotations": ["org.jetbrains:annotations"],
        "AndroidX Support Components": ["Other AndroidX support components"],
    }

    # Helper to find notice entry
    def find_notice_entry(name: str):
        tokens = dep_tokens.get(name, [name])
        for r in raw_notice_entries:
            if any(t.lower() in r.lower() for t in tokens):
                return r
        return ""

    entries = []

    # 1. Parse dependencies from docs/release/dependency-licences.txt
    dep_lines = [l.strip() for l in dep_file.read_text(encoding="utf-8").splitlines() if l.strip() and not l.startswith("#")]
    for line in dep_lines:
        parts = [p.strip() for p in line.split("|")]
        name = parts[0]
        licence = parts[1]
        coord = parts[2] if len(parts) > 2 else ""

        # Version extraction from coordinate
        version = ""
        if ":" in coord:
            v_match = re.search(r":([0-9][0-9a-zA-Z._-]+)$", coord)
            if v_match:
                version = v_match.group(1)

        # Match to NOTICE block
        notice_block = find_notice_entry(name)
        url = ""
        for bl in notice_block.splitlines():
            if bl.strip().startswith("http"):
                url = bl.strip()
                break

        category = "Fonts and designs" if name == "Roboto Flex" else "Built with"

        # Determine full licence text
        full_text = ""
        if name == "Roboto Flex":
            ofl_path = licenses_dir / "roboto-flex-OFL.txt"
            if ofl_path.exists():
                full_text = ofl_path.read_text(encoding="utf-8").strip()
        elif "shaded protobuf" in name.lower():
            # Protobuf has full BSD license in NOTICE
            full_text = notice_block.strip()
        elif "bouncy castle" in name.lower():
            full_text = notice_block.strip()
        elif licence == "Apache-2.0":
            full_text = f"{notice_block.strip()}\n\n{apache_2_notice}"
        else:
            full_text = notice_block.strip()

        entries.append({
            "name": name,
            "version": version,
            "licence": licence,
            "url": url,
            "category": category,
            "fullLicenceText": full_text
        })

    # 2. Add the 9 palettes
    palette_file_names = {
        "Catppuccin": "catppuccin-LICENSE.txt",
        "Gruvbox": "gruvbox-LICENSE.txt",
        "Nord": "nord-LICENSE.txt",
        "Rosé Pine": "rosepine-LICENSE.txt",
        "Solarized": "solarized-LICENSE.txt",
        "Dracula": "dracula-LICENSE.txt",
        "Tokyo Night": "tokyonight-LICENSE.txt",
        "Everforest": "everforest-LICENSE.txt",
        "Kanagawa": "kanagawa-LICENSE.txt",
    }

    for p_name, file_name in palette_file_names.items():
        lic_file = licenses_dir / "palettes" / file_name
        full_text = ""
        if lic_file.exists():
            full_text = lic_file.read_text(encoding="utf-8").strip()

        _, url = palette_info.get(p_name, ("", ""))
        if not url:
            # Fallback for unicode variations
            for k, v in palette_info.items():
                if "rose" in k.lower() and "rose" in p_name.lower():
                    url = v[1]
                    break

        entries.append({
            "name": p_name,
            "version": "",
            "licence": "MIT",
            "url": url,
            "category": "Fonts and designs",
            "fullLicenceText": full_text
        })

    output_file.parent.mkdir(parents=True, exist_ok=True)
    output_json = json.dumps(entries, indent=2, ensure_ascii=False)
    output_file.write_text(output_json + "\n", encoding="utf-8")
    print(f"Generated {output_file} with {len(entries)} entries.")

if __name__ == "__main__":
    main()
