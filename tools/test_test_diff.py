#!/usr/bin/env python3
"""
Unit and fixture tests for tools/test-diff.py.
Run from repository root:
    python tools/test_test_diff.py
"""

import os
import subprocess
import sys
import tempfile

CHECKER = os.path.join(os.path.dirname(os.path.abspath(__file__)), "test-diff.py")
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def run_git(cmd, cwd):
    result = subprocess.run(["git"] + cmd, cwd=cwd, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(f"git {' '.join(cmd)} failed: {result.stderr}")
    return result.stdout.strip()


def run_tool(args, cwd=ROOT):
    return subprocess.run([sys.executable, CHECKER] + args, cwd=cwd, capture_output=True, text=True)


INITIAL_KOTLIN = """package sample

import org.junit.Test
import org.junit.Assert.assertEquals

class SampleTest {
    @Test
    fun test_to_delete() {
        assertEquals(1, 1)
    }

    @Test
    fun test_to_rename() {
        val x = 42
        assertEquals(42, x)
    }

    @Test
    fun test_to_shrink() {
        assertEquals(1, 1)
        assertEquals(2, 2)
        assertEquals(3, 3)
        assertEquals(4, 4)
        assertEquals(5, 5)
    }

    @Test
    fun test_unchanged() {
        assertEquals(0, 0)
    }
}
"""

MODIFIED_KOTLIN = """package sample

import org.junit.Test
import org.junit.Assert.assertEquals

class SampleTest {
    @Test
    fun test_renamed() {
        val x = 42
        assertEquals(42, x)
    }

    @Test
    fun test_to_shrink() {
        assertEquals(1, 1)
        assertEquals(2, 2)
        assertEquals(3, 3)
    }

    @Test
    fun test_unchanged() {
        assertEquals(0, 0)
    }

    @Test
    fun test_added() {
        assertEquals(99, 99)
    }
}
"""


def main() -> int:
    failures = 0

    with tempfile.TemporaryDirectory() as tmp:
        # Initialize temp git repo
        run_git(["init", "-b", "main"], cwd=tmp)
        run_git(["config", "user.name", "Test User"], cwd=tmp)
        run_git(["config", "user.email", "test@example.com"], cwd=tmp)

        test_file = os.path.join(tmp, "SampleTest.kt")
        with open(test_file, "w", encoding="utf-8") as f:
            f.write(INITIAL_KOTLIN)

        run_git(["add", "SampleTest.kt"], cwd=tmp)
        run_git(["commit", "-m", "initial test file"], cwd=tmp)
        base_commit = run_git(["rev-parse", "HEAD"], cwd=tmp)

        with open(test_file, "w", encoding="utf-8") as f:
            f.write(MODIFIED_KOTLIN)

        run_git(["commit", "-am", "modified test file"], cwd=tmp)
        head_commit = run_git(["rev-parse", "HEAD"], cwd=tmp)

        # 1. Run without --allow (should fail with exit code 1)
        res_no_allow = run_tool([base_commit, head_commit], cwd=tmp)
        expected_lines = [
            "REMOVED SampleTest.kt::test_to_delete",
            "RENAMED SampleTest.kt::test_to_rename -> test_renamed",
            "SHRUNK SampleTest.kt::test_to_shrink asserts 5->3",
            "ADDED 1",
        ]
        
        ok_exit = res_no_allow.returncode == 1
        output = res_no_allow.stdout
        lines_present = all(line in output for line in expected_lines)

        if ok_exit and lines_present:
            print("PASS: test-diff without --allow exits 1 and lists all changes")
        else:
            print(f"FAIL: test-diff without --allow (exit {res_no_allow.returncode}, expected 1)")
            print(f"Stdout:\n{output}\nStderr:\n{res_no_allow.stderr}")
            failures += 1

        # 2. Run with complete --allow (should pass with exit code 0)
        allow_file = os.path.join(tmp, "allow.txt")
        with open(allow_file, "w", encoding="utf-8") as f:
            f.write("SampleTest.kt::test_to_delete  obsolete test\n")
            f.write("SampleTest.kt::test_to_shrink  refactored assertions\n")

        res_allow = run_tool([base_commit, head_commit, "--allow", allow_file], cwd=tmp)
        ok_exit_allow = res_allow.returncode == 0
        lines_present_allow = all(line in res_allow.stdout for line in expected_lines)

        if ok_exit_allow and lines_present_allow:
            print("PASS: test-diff with complete --allow exits 0")
        else:
            print(f"FAIL: test-diff with complete --allow (exit {res_allow.returncode}, expected 0)")
            print(f"Stdout:\n{res_allow.stdout}\nStderr:\n{res_allow.stderr}")
            failures += 1

        # 3. Run with partial --allow (missing test_to_shrink, should exit 1)
        partial_allow_file = os.path.join(tmp, "partial_allow.txt")
        with open(partial_allow_file, "w", encoding="utf-8") as f:
            f.write("SampleTest.kt::test_to_delete  obsolete test\n")

        res_partial = run_tool([base_commit, head_commit, "--allow", partial_allow_file], cwd=tmp)
        if res_partial.returncode == 1:
            print("PASS: test-diff with partial --allow exits 1")
        else:
            print(f"FAIL: test-diff with partial --allow (exit {res_partial.returncode}, expected 1)")
            failures += 1

    print(f"{failures} failure(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
