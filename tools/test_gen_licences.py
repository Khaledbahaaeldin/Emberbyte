#!/usr/bin/env python3
"""Tests for the content digest that keeps app/src/main/assets/licences.json in step with its sources."""
import importlib.util
import os
import tempfile
import unittest
from pathlib import Path

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("gen_licences", HERE / "gen-licences.py")
gen = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gen)


def make_tree(root: Path, dep: bytes = b"a\nb\n", notice: bytes = b"n\n", lic: bytes = b"l\n"):
    (root / "docs" / "release").mkdir(parents=True)
    (root / "licenses" / "palettes").mkdir(parents=True)
    (root / "docs" / "release" / "dependency-licences.txt").write_bytes(dep)
    (root / "NOTICE").write_bytes(notice)
    (root / "licenses" / "palettes" / "x.txt").write_bytes(lic)


class SourcesDigestTest(unittest.TestCase):
    def digest(self, **kw):
        with tempfile.TemporaryDirectory() as d:
            make_tree(Path(d), **kw)
            return gen.sources_digest(Path(d))

    def test_same_content_gives_the_same_digest(self):
        self.assertEqual(self.digest(), self.digest())

    def test_line_endings_do_not_matter(self):
        self.assertEqual(self.digest(dep=b"a\nb\n"), self.digest(dep=b"a\r\nb\r\n"))

    def test_changing_any_source_changes_the_digest(self):
        base = self.digest()
        self.assertNotEqual(base, self.digest(dep=b"a\nc\n"))
        self.assertNotEqual(base, self.digest(notice=b"m\n"))
        self.assertNotEqual(base, self.digest(lic=b"k\n"))

    def test_file_modification_times_do_not_matter(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            make_tree(root)
            first = gen.sources_digest(root)
            for f in root.rglob("*"):
                if f.is_file():
                    os.utime(f, (1, 1))
            self.assertEqual(first, gen.sources_digest(root))

    def test_the_committed_digest_matches_the_repository(self):
        root = HERE.parent
        committed = (root / "app" / "licences.sources.sha256").read_text(encoding="utf-8").strip()
        self.assertEqual(committed, gen.sources_digest(root),
                         "Run python tools/gen-licences.py and commit app/licences.sources.sha256")


if __name__ == "__main__":
    unittest.main()
