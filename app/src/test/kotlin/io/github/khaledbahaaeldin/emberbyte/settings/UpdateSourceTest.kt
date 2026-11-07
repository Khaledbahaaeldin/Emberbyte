package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdateSourceTest {
    @Test fun classifies_the_known_updaters() {
        assertEquals(InstallerKind.Obtainium, classifyInstaller("dev.imranr.obtainium"))
        assertEquals(InstallerKind.Obtainium, classifyInstaller("dev.imranr.obtainium.fdroid"))
        assertEquals(InstallerKind.OrionStore, classifyInstaller("com.orion.store"))
        assertEquals(InstallerKind.FDroid, classifyInstaller("org.fdroid.fdroid"))
        assertEquals(InstallerKind.FDroid, classifyInstaller("org.fdroid.basic"))
    }

    @Test fun null_and_blank_are_unknown() {
        assertEquals(InstallerKind.Unknown, classifyInstaller(null))
        assertEquals(InstallerKind.Unknown, classifyInstaller(""))
        assertEquals(InstallerKind.Unknown, classifyInstaller("   "))
    }

    @Test fun an_unrecognised_package_is_other() {
        assertEquals(InstallerKind.Other, classifyInstaller("com.android.vending"))
        assertEquals(InstallerKind.Other, classifyInstaller("com.google.android.packageinstaller"))
    }

    @Test fun reader_falls_back_to_unknown_when_the_lookup_throws() {
        val reader = InstallSourceReader { throw SecurityException("no access") }
        assertEquals(InstallerKind.Unknown, reader.kind())
        assertNull(reader.installerPackage())
    }

    @Test fun reader_uses_the_install_source_on_api_35() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        shadowOf(context.packageManager).setInstallSourceInfo(context.packageName, null, "dev.imranr.obtainium")
        assertEquals(InstallerKind.Obtainium, InstallSourceReader.forContext(context).kind())
    }

    @Test fun reader_uses_the_installer_package_name_on_api_29() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        @Suppress("DEPRECATION")
        context.packageManager.setInstallerPackageName(context.packageName, "com.orion.store")
        assertEquals(InstallerKind.OrionStore, InstallSourceReader.forContext(context, sdkInt = 29).kind())
    }

    @Test fun an_app_with_no_installer_is_unknown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(InstallerKind.Unknown, InstallSourceReader.forContext(context).kind())
    }
}
