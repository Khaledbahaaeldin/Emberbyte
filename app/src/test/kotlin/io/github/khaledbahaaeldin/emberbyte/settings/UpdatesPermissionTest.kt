package io.github.khaledbahaaeldin.emberbyte.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

/** The Updates row works without the network: the manifest must never gain a permission for it. */
class UpdatesPermissionTest {
    private val allowed = setOf(
        "android.permission.PACKAGE_USAGE_STATS",
        "android.permission.QUERY_ALL_PACKAGES",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.READ_PHONE_STATE",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.POST_PROMOTED_NOTIFICATIONS",
        "android.permission.FOREGROUND_SERVICE",
        "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
        "android.permission.RECEIVE_BOOT_COMPLETED",
    )

    @Test fun the_manifest_declares_only_the_known_permissions_and_never_internet() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val declared = Regex("<uses-permission[^>]*android:name=\"([^\"]+)\"")
            .findAll(manifest).map { it.groupValues[1] }.toSet()
        assertFalse("INTERNET must never be declared", "android.permission.INTERNET" in declared)
        assertEquals("Unexpected permission set: $declared", allowed, declared)
    }
}
