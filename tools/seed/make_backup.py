#!/usr/bin/env python3
"""
Emberbyte Seeded Backup Tool
Generates encrypted .emberbyte backups adhering to docs/backup-format.md.

Usage:
    python tools/seed/make_backup.py --out FILE --passphrase TEXT [--days N] [--scenario basic|midcycle] [--selftest]
"""

import argparse
from datetime import datetime, timedelta, timezone
import gzip
import json
import os
import struct
import sys

try:
    from argon2 import low_level
    from cryptography.hazmat.primitives.ciphers.aead import AESGCM
except ImportError:
    print(
        "Missing required dependencies. Install them with:\n"
        "    pip install argon2-cffi cryptography",
        file=sys.stderr,
    )
    sys.exit(1)

MAGIC = b"EMBBKP01"
HEADER_SIZE = 48
TAG_SIZE = 16

DEFAULT_MEMORY_KIB = 65536
DEFAULT_ITERATIONS = 3
DEFAULT_PARALLELISM = 1

FAKE_PACKAGES = [
    "com.fake.browser",
    "com.fake.video",
    "com.fake.social",
    "com.fake.music",
    "com.fake.chat",
    "com.fake.cloud",
]


def generate_backup_payload(days: int = 7, scenario: str = "basic") -> dict:
    # 1 plan: cap 20 GB (21474836480 bytes), monthly on day 1
    plan = {
        "id": 1,
        "name": "Primary Plan",
        "subscriptionId": 1,
        "capBytes": 21474836480,
        "cycle": {
            "type": "monthlyOnDay",
            "dayOfMonth": 1,
            "time": "00:00",
            "zone": "UTC",
        },
        "rollover": {
            "type": "none",
        },
        "archived": False,
        "addOns": [],
        "freeRules": [],
        "alertRules": [
            {
                "id": 1,
                "planId": 1,
                "thresholdPercent": 80,
                "enabled": True,
            }
        ],
    }

    # 1 budget: daily limit on video package
    budget = {
        "id": 1,
        "package": "com.fake.video",
        "limitBytes": 5368709120,
        "period": "DAILY",
        "planId": None,
        "warnAtPercent": 80,
        "enabled": True,
    }

    # Hourly usage for fake packages across N days
    end_time = datetime(2026, 10, 17, 0, 0, 0, tzinfo=timezone.utc)
    start_time = end_time - timedelta(days=days)
    total_hours = days * 24

    usage_hourly = []
    for h in range(total_hours):
        hour_dt = start_time + timedelta(hours=h)
        hour_str = hour_dt.strftime("%Y-%m-%dT%H:%M:%SZ")
        for pkg_idx, pkg in enumerate(FAKE_PACKAGES):
            rx = 100_000 + ((h * 7 + pkg_idx * 31) % 500) * 10_000
            tx = rx // 8
            usage_hourly.append({
                "hourStart": hour_str,
                "package": pkg,
                "network": "MOBILE",
                "subscriptionId": 1,
                "rxBytes": rx,
                "txBytes": tx,
            })

    return {
        "contractVersion": 1,
        "exportedAt": "2026-10-17T00:00:00Z",
        "plans": [plan],
        "appBudgets": [budget],
        "usageHourly": usage_hourly,
    }


def encrypt_backup(
    payload: dict,
    passphrase: str,
    memory_kib: int = DEFAULT_MEMORY_KIB,
    iterations: int = DEFAULT_ITERATIONS,
    parallelism: int = DEFAULT_PARALLELISM,
) -> bytes:
    salt = os.urandom(16)
    nonce = os.urandom(12)

    header = (
        MAGIC
        + salt
        + struct.pack(">3i", memory_kib, iterations, parallelism)
        + nonce
    )

    key = low_level.hash_secret_raw(
        secret=passphrase.encode("utf-8"),
        salt=salt,
        time_cost=iterations,
        memory_cost=memory_kib,
        parallelism=parallelism,
        hash_len=32,
        type=low_level.Type.ID,
    )

    json_bytes = json.dumps(payload, separators=(",", ":")).encode("utf-8")
    compressed = gzip.compress(json_bytes)

    aesgcm = AESGCM(key)
    ciphertext_and_tag = aesgcm.encrypt(nonce, compressed, header)

    return header + ciphertext_and_tag


def decrypt_backup(data: bytes, passphrase: str) -> dict:
    if len(data) < HEADER_SIZE + TAG_SIZE:
        raise ValueError("File is too small to be an Emberbyte backup")

    if data[:8] != MAGIC:
        raise ValueError(f"Invalid magic header: expected {MAGIC!r}, got {data[:8]!r}")

    header = data[:HEADER_SIZE]
    salt = data[8:24]
    memory_kib, iterations, parallelism = struct.unpack(">3i", data[24:36])
    nonce = data[36:48]
    ciphertext_and_tag = data[48:]

    key = low_level.hash_secret_raw(
        secret=passphrase.encode("utf-8"),
        salt=salt,
        time_cost=iterations,
        memory_cost=memory_kib,
        parallelism=parallelism,
        hash_len=32,
        type=low_level.Type.ID,
    )

    aesgcm = AESGCM(key)
    compressed_plaintext = aesgcm.decrypt(nonce, ciphertext_and_tag, header)
    json_bytes = gzip.decompress(compressed_plaintext)
    return json.loads(json_bytes.decode("utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate Emberbyte seed backups")
    parser.add_argument("--out", help="Output .emberbyte file path")
    parser.add_argument("--passphrase", default="test-passphrase", help="Passphrase for encryption")
    parser.add_argument("--days", type=int, default=7, help="Number of days of usage history (default: 7)")
    parser.add_argument("--scenario", choices=["basic", "midcycle"], default="basic", help="Scenario to generate")
    parser.add_argument("--selftest", action="store_true", help="Run self-test verifying decryption")

    args = parser.parse_args()

    if not args.out and not args.selftest:
        parser.error("--out FILE is required unless --selftest is specified")

    payload = generate_backup_payload(days=args.days, scenario=args.scenario)
    row_count = len(payload["usageHourly"])
    print(f"rows={row_count}")

    encrypted = encrypt_backup(payload, args.passphrase)

    if args.out:
        out_dir = os.path.dirname(os.path.abspath(args.out))
        if out_dir:
            os.makedirs(out_dir, exist_ok=True)
        with open(args.out, "wb") as f:
            f.write(encrypted)
        print(f"Wrote {len(encrypted)} bytes to {args.out}")

    if args.selftest:
        decrypted = decrypt_backup(encrypted, args.passphrase)
        assert decrypted["contractVersion"] == 1, "contractVersion mismatch"
        assert len(decrypted["plans"]) == 1, "plans count mismatch"
        assert len(decrypted["appBudgets"]) == 1, "budgets count mismatch"
        assert len(decrypted["usageHourly"]) == row_count, "usage rows mismatch"
        assert decrypted["plans"][0]["capBytes"] == 21474836480, "plan capBytes mismatch"
        print("Self-test passed!")

    return 0


if __name__ == "__main__":
    sys.exit(main())
