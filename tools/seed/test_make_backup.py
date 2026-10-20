#!/usr/bin/env python3
"""
Unit tests for tools/seed/make_backup.py
"""

import os
import sys
import unittest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..")))

from tools.seed.make_backup import (
    DEFAULT_WEIGHTS,
    FAKE_PACKAGES,
    decrypt_backup,
    encrypt_backup,
    generate_backup_payload,
)


class MakeBackupTest(unittest.TestCase):
    def test_default_payload_structure(self):
        payload = generate_backup_payload(days=2)
        self.assertEqual(payload["contractVersion"], 1)
        self.assertEqual(len(payload["plans"]), 1)
        self.assertEqual(len(payload["appBudgets"]), 1)
        # 2 days * 24 hours * 6 fake packages = 288 rows
        self.assertEqual(len(payload["usageHourly"]), 2 * 24 * len(FAKE_PACKAGES))

    def test_varied_weights_produces_varied_totals(self):
        pkgs = ["pkg.a", "pkg.b", "pkg.c", "pkg.d", "pkg.e"]
        weights = [2.7, 1.44, 0.92, 0.52, 0.20]
        payload = generate_backup_payload(days=7, packages=pkgs, weights=weights)

        totals = {p: 0 for p in pkgs}
        for row in payload["usageHourly"]:
            totals[row["package"]] += row["rxBytes"] + row["txBytes"]

        # Check descending order of totals
        pkg_order = [p for p, _ in sorted(totals.items(), key=lambda kv: kv[1], reverse=True)]
        self.assertEqual(pkg_order, pkgs)
        # Check non-zero usage
        for p in pkgs:
            self.assertGreater(totals[p], 0)

    def test_encryption_roundtrip(self):
        payload = generate_backup_payload(days=1)
        passphrase = "unit-test-passphrase"
        encrypted = encrypt_backup(payload, passphrase)
        self.assertGreater(len(encrypted), 64)

        decrypted = decrypt_backup(encrypted, passphrase)
        self.assertEqual(decrypted["contractVersion"], payload["contractVersion"])
        self.assertEqual(len(decrypted["usageHourly"]), len(payload["usageHourly"]))
        self.assertEqual(len(decrypted["plans"]), len(payload["plans"]))


if __name__ == "__main__":
    unittest.main()
