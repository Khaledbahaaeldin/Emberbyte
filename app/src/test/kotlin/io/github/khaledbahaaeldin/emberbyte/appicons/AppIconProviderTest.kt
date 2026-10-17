package io.github.khaledbahaaeldin.emberbyte.appicons

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppIconProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val packageManager = context.packageManager
    private val shadowPm = shadowOf(packageManager)

    @Test
    fun returns_a_96px_bitmap_for_an_installed_package() = runTest {
        val pkg = "com.example.installed"
        shadowPm.installPackage(PackageInfo().apply { packageName = pkg })
        shadowPm.setApplicationIcon(pkg, ColorDrawable(Color.RED))

        val provider = AppIconProvider(context)
        val bitmap = provider.icon(pkg)

        assertNotNull("Expected icon bitmap to not be null", bitmap)
        assertEquals(96, bitmap!!.width)
        assertEquals(96, bitmap.height)
    }

    @Test
    fun returns_null_for_uid_pseudo_packages() = runTest {
        val provider = AppIconProvider(context)
        assertNull(provider.icon("uid:1000"))
        assertNull(provider.icon("uid:root"))
    }

    @Test
    fun returns_null_for_an_unknown_package_and_does_not_throw() = runTest {
        val provider = AppIconProvider(context)
        val bitmap = provider.icon("com.nonexistent.unknown.app")
        assertNull(bitmap)
    }

    @Test
    fun second_call_is_served_from_the_cache() = runTest {
        val pkg = "com.example.cached"
        var loadCount = 0
        val drawable = ColorDrawable(Color.BLUE)
        val provider = AppIconProvider(
            packageManager = packageManager,
            iconLoader = {
                loadCount++
                drawable
            },
        )

        val first = provider.icon(pkg)
        assertNotNull(first)
        assertEquals(1, loadCount)

        val second = provider.icon(pkg)
        assertNotNull(second)
        assertEquals(1, loadCount)
        assertEquals(first, second)
    }

    @Test
    fun cache_holds_64_entries_and_evicts_the_oldest() = runTest {
        var loadCount = 0
        val drawable = ColorDrawable(Color.GREEN)
        val provider = AppIconProvider(
            packageManager = packageManager,
            iconLoader = {
                loadCount++
                drawable
            },
        )

        for (i in 0 until 64) {
            val icon = provider.icon("com.example.app$i")
            assertNotNull(icon)
        }
        assertEquals(64, loadCount)

        // Load 65th package, evicts com.example.app0
        val icon64 = provider.icon("com.example.app64")
        assertNotNull(icon64)
        assertEquals(65, loadCount)

        // Loading com.example.app0 again must invoke loader (evicted)
        val icon0Reloaded = provider.icon("com.example.app0")
        assertNotNull(icon0Reloaded)
        assertEquals(66, loadCount)

        // Loading com.example.app64 again must be cached
        val icon64Again = provider.icon("com.example.app64")
        assertNotNull(icon64Again)
        assertEquals(66, loadCount)
    }

    @Test
    fun a_huge_icon_is_scaled_to_96px() = runTest {
        val pkg = "com.example.huge"
        val bigBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val hugeDrawable = BitmapDrawable(context.resources, bigBitmap)
        shadowPm.installPackage(PackageInfo().apply { packageName = pkg })
        shadowPm.setApplicationIcon(pkg, hugeDrawable)

        val provider = AppIconProvider(context)
        val bitmap = provider.icon(pkg)

        assertNotNull(bitmap)
        assertEquals(96, bitmap!!.width)
        assertEquals(96, bitmap.height)
    }
}
