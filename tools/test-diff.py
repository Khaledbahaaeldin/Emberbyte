#!/usr/bin/env python3
"""
tools/test-diff.py <base> <head> [--allow FILE]

Lists every test function (a `fun` preceded by `@Test`) that exists at <base>
and is missing or renamed at <head> (match by file and by name; detect a rename
by identical body), every test whose assertions shrank (fewer `assert*` calls),
and every added test; prints counts.

Exit 1 when a REMOVED or SHRUNK entry is not listed in the --allow file.
"""

import argparse
import os
import re
import subprocess
import sys
from typing import Dict, List, NamedTuple, Optional, Set, Tuple


class TestFunc(NamedTuple):
    name: str
    body: str
    assert_count: int


def run_git(cmd: List[str], cwd: Optional[str] = None) -> str:
    res = subprocess.run(["git"] + cmd, cwd=cwd, capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"git {' '.join(cmd)} failed: {res.stderr.strip()}")
    return res.stdout


def strip_comments(text: str) -> str:
    # Strip block comments
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.DOTALL)
    # Strip line comments
    text = re.sub(r'(?<!:)//[^\n]*', '', text)
    return text


def extract_balanced_body(text: str, start_index: int) -> Optional[Tuple[str, int]]:
    """Find the opening brace '{' after start_index and extract until matching '}'."""
    i = start_index
    n = len(text)
    brace_start = -1

    while i < n:
        c = text[i]
        if c == '{':
            brace_start = i
            break
        i += 1

    if brace_start == -1:
        return None

    depth = 0
    in_single_quote = False
    in_double_quote = False
    in_triple_quote = False

    i = brace_start
    while i < n:
        if in_triple_quote:
            if text[i:i + 3] == '"""':
                in_triple_quote = False
                i += 3
                continue
            i += 1
            continue

        if in_double_quote:
            if text[i] == '\\':
                i += 2
                continue
            if text[i] == '"':
                in_double_quote = False
            i += 1
            continue

        if in_single_quote:
            if text[i] == '\\':
                i += 2
                continue
            if text[i] == "'":
                in_single_quote = False
            i += 1
            continue

        # Check start of string literals
        if text[i:i + 3] == '"""':
            in_triple_quote = True
            i += 3
            continue
        elif text[i] == '"':
            in_double_quote = True
            i += 1
            continue
        elif text[i] == "'":
            in_single_quote = True
            i += 1
            continue

        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
            if depth == 0:
                body = text[brace_start:i + 1]
                return body, i + 1
        i += 1

    return None


def count_asserts(body: str) -> int:
    return len(re.findall(r'\bassert\w*\s*\(', body))


def parse_test_file(content: str) -> Dict[str, TestFunc]:
    tests: Dict[str, TestFunc] = {}
    clean_content = strip_comments(content)

    test_pattern = re.compile(
        r'@(?:[A-Za-z0-9_.]+\.)?Test\b(?:\([^)]*\))?(?:(?!\bfun\b)[^;{}])*?\bfun\s+(?:`([^`\n]+)`|([A-Za-z0-9_]+))'
    )

    for m in test_pattern.finditer(clean_content):
        name = m.group(1) or m.group(2)
        end_match = m.end()

        # Find param list closing ')'
        paren_idx = clean_content.find('(', end_match)
        if paren_idx == -1:
            body_start = end_match
        else:
            depth = 0
            body_start = paren_idx
            for k in range(paren_idx, len(clean_content)):
                if clean_content[k] == '(':
                    depth += 1
                elif clean_content[k] == ')':
                    depth -= 1
                    if depth == 0:
                        body_start = k + 1
                        break

        extracted = extract_balanced_body(clean_content, body_start)
        if extracted:
            body, _ = extracted
            asserts = count_asserts(body)
            tests[name] = TestFunc(name=name, body=body, assert_count=asserts)

    return tests


def normalize_body(body: str) -> str:
    return re.sub(r'\s+', ' ', body.strip())


def get_test_files(tree_ish: str, cwd: Optional[str] = None) -> List[str]:
    output = run_git(["ls-tree", "-r", "--name-only", tree_ish], cwd=cwd)
    files = [f.strip().replace("\\", "/") for f in output.splitlines() if f.strip().endswith("Test.kt")]
    return sorted(files)


def get_file_content(tree_ish: str, path: str, cwd: Optional[str] = None) -> str:
    return run_git(["show", f"{tree_ish}:{path}"], cwd=cwd)


def main() -> int:
    parser = argparse.ArgumentParser(description="Diff tests between two git tree-ishes")
    parser.add_argument("base", help="Base commit / branch / tag")
    parser.add_argument("head", nargs="?", default="HEAD", help="Head commit / branch / tag (default: HEAD)")
    parser.add_argument("--allow", help="File listing allowed REMOVED and SHRUNK tests")
    args = parser.parse_args()

    allowed_entries: Set[str] = set()
    if args.allow and os.path.exists(args.allow):
        with open(args.allow, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                parts = line.split()
                if parts:
                    allowed_entries.add(parts[0])

    base_files = set(get_test_files(args.base))
    head_files = set(get_test_files(args.head))
    all_files = sorted(base_files | head_files)

    removed: List[Tuple[str, str]] = []
    renamed: List[Tuple[str, str, str]] = []
    shrunk: List[Tuple[str, str, int, int]] = []
    total_added = 0

    for path in all_files:
        base_tests: Dict[str, TestFunc] = {}
        head_tests: Dict[str, TestFunc] = {}

        if path in base_files:
            try:
                base_content = get_file_content(args.base, path)
                base_tests = parse_test_file(base_content)
            except Exception:
                base_tests = {}

        if path in head_files:
            try:
                head_content = get_file_content(args.head, path)
                head_tests = parse_test_file(head_content)
            except Exception:
                head_tests = {}

        # 1. Tests in both: check for shrunk asserts
        for name in base_tests:
            if name in head_tests:
                base_asserts = base_tests[name].assert_count
                head_asserts = head_tests[name].assert_count
                if head_asserts < base_asserts:
                    shrunk.append((path, name, base_asserts, head_asserts))

        # 2. Missing from head
        missing_names = [name for name in base_tests if name not in head_tests]
        # 3. Added to head
        added_names = [name for name in head_tests if name not in base_tests]

        # 4. Check for renames within the file (identical body)
        matched_missing: Set[str] = set()
        matched_added: Set[str] = set()

        for old_name in missing_names:
            old_body_norm = normalize_body(base_tests[old_name].body)
            for new_name in added_names:
                if new_name not in matched_added:
                    new_body_norm = normalize_body(head_tests[new_name].body)
                    if old_body_norm == new_body_norm:
                        renamed.append((path, old_name, new_name))
                        matched_missing.add(old_name)
                        matched_added.add(new_name)
                        break

        # 5. Remaining missing are REMOVED
        for old_name in missing_names:
            if old_name not in matched_missing:
                removed.append((path, old_name))

        # 6. Remaining added count towards ADDED
        for new_name in added_names:
            if new_name not in matched_added:
                total_added += 1

    # Print output
    for path, name in sorted(removed):
        print(f"REMOVED {path}::{name}")

    for path, old, new in sorted(renamed):
        print(f"RENAMED {path}::{old} -> {new}")

    for path, name, old_c, new_c in sorted(shrunk):
        print(f"SHRUNK {path}::{name} asserts {old_c}->{new_c}")

    print(f"ADDED {total_added}")

    # Check against allow list
    disallowed: List[str] = []
    for path, name in removed:
        key = f"{path}::{name}"
        if key not in allowed_entries:
            disallowed.append(key)

    for path, name, _, _ in shrunk:
        key = f"{path}::{name}"
        if key not in allowed_entries:
            disallowed.append(key)

    if disallowed:
        return 1

    return 0


if __name__ == "__main__":
    sys.exit(main())
