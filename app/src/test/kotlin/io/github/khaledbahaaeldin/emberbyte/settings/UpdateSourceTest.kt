package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdateSourceTest {

    @Test
    fun classifyInstaller_recognizes_obtainium() {
        assertEquals(UpdateSource.Obtainium, classifyInstaller("dev.imranr.obtainium"))
        assertEquals(UpdateSource.Obtainium, classifyInstaller("dev.imranr.obtainium.fdroid"))
    }

    @Test
    fun classifyInstaller_recognizes_orion_store() {
        assertEquals(UpdateSource.OrionStore, classifyInstaller("com.orion.store"))
    }

    @Test
    fun classifyInstaller_recognizes_fdroid() {
        assertEquals(UpdateSource.FDroid, classifyInstaller("org.fdroid.fdroid"))
        assertEquals(UpdateSource.FDroid, classifyInstaller("org.fdroid.basic"))
        assertEquals(UpdateSource.FDroid, classifyInstaller("org.fdroid.fdroid.privileged"))
        assertEquals(UpdateSource.FDroid, classifyInstaller("org.fdroid.fdroid.classic"))
    }

    @Test
    fun classifyInstaller_recognizes_unknown() {
        assertEquals(UpdateSource.Unknown, classifyInstaller(null))
        assertEquals(UpdateSource.Unknown, classifyInstaller(""))
        assertEquals(UpdateSource.Unknown, classifyInstaller("   "))
    }

    @Test
    fun classifyInstaller_recognizes_other() {
        assertEquals(UpdateSource.Other, classifyInstaller("com.android.vending"))
        assertEquals(UpdateSource.Other, classifyInstaller("com.google.android.packageinstaller"))
        assertEquals(UpdateSource.Other, classifyInstaller("com.aurora.store"))
        assertEquals(UpdateSource.Other, classifyInstaller("org.custom.store"))
    }

    @Test
    @Config(sdk = [34])
    fun installSourceReader_api30_plus_reads_installing_package() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = shadowOf(context.packageManager)
        shadowPm.setInstallSourceInfo(
            context.packageName,
            "initiating.pkg",
            "com.orion.store"
        )

        val installer = InstallSourceReader.getInstallerPackageName(context)
        assertEquals("com.orion.store", installer)
        assertEquals(UpdateSource.OrionStore, InstallSourceReader.getUpdateSource(context))
    }

    @Test
    @Config(sdk = [34])
    fun installSourceReader_api30_plus_prioritizes_known_updater_initiating_package() {
        // When Obtainium or F-Droid initiates an install, system packageinstaller is often the installing package
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = shadowOf(context.packageManager)
        shadowPm.setInstallSourceInfo(
            context.packageName,
            "dev.imranr.obtainium",
            "com.google.android.packageinstaller"
        )

        val installer = InstallSourceReader.getInstallerPackageName(context)
        assertEquals("dev.imranr.obtainium", installer)
        assertEquals(UpdateSource.Obtainium, InstallSourceReader.getUpdateSource(context))
    }

    @Test
    @Config(sdk = [34])
    fun installSourceReader_api30_plus_falls_back_to_initiating_package_when_installing_null() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = shadowOf(context.packageManager)
        shadowPm.setInstallSourceInfo(
            context.packageName,
            "dev.imranr.obtainium",
            null
        )

        val installer = InstallSourceReader.getInstallerPackageName(context)
        assertEquals("dev.imranr.obtainium", installer)
        assertEquals(UpdateSource.Obtainium, InstallSourceReader.getUpdateSource(context))
    }

    @Test
    fun installSourceReader_api29_fallback_reads_installer_package_name() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.packageManager.setInstallerPackageName(context.packageName, "org.fdroid.fdroid")

        val installer = InstallSourceReader.resolveInstaller(context.packageManager, context.packageName, sdkInt = 29)
        assertEquals("org.fdroid.fdroid", installer)
        assertEquals(UpdateSource.FDroid, classifyInstaller(installer))
    }

    @Test
    fun installSourceReader_never_throws_on_exceptions() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val throwingContext = object : ContextWrapper(baseContext) {
            override fun getPackageManager(): PackageManager {
                throw SecurityException("Mock security exception")
            }
        }

        assertNull(InstallSourceReader.getInstallerPackageName(throwingContext))
        assertEquals(UpdateSource.Unknown, InstallSourceReader.getUpdateSource(throwingContext))
    }

    @Test
    fun manifest_declares_no_extra_permissions_and_no_internet() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val permissions = packageInfo.requestedPermissions?.toSet().orEmpty()

        assertFalse("Must NOT declare INTERNET permission", permissions.contains("android.permission.INTERNET"))
        assertFalse("Must NOT declare BIND_VPN_SERVICE", permissions.any { it.contains("VPN", ignoreCase = true) })

        val expectedPermissions = setOf(
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.POST_PROMOTED_NOTIFICATIONS",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.QUERY_ALL_PACKAGES",
            "android.permission.WAKE_LOCK",
            "io.github.khaledbahaaeldin.emberbyte.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        )

        assertEquals("Manifest permissions must exactly match expected permissions set", expectedPermissions, permissions)
    }
}
