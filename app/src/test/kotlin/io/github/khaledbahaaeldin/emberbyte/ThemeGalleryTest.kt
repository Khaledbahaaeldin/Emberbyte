package io.github.khaledbahaaeldin.emberbyte

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.ThemePresets
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1200dp")
class ThemeGalleryTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun renders_every_theme_in_both_modes() {
        var currentTheme by mutableStateOf("dusk")
        var currentDark by mutableStateOf(true)

        rule.setContent {
            ThemeGalleryContent(themeId = currentTheme, dark = currentDark, amoled = false)
        }

        val themeIds = ThemePresets.map { it.id }
        for (id in themeIds) {
            for (dark in listOf(true, false)) {
                currentTheme = id
                currentDark = dark
                rule.waitForIdle()

                rule.onNodeWithTag("theme_gallery_root").assertIsDisplayed()
                rule.onNodeWithContentDescription("1.38 gigabytes used today, 12.4 gigabytes left").assertIsDisplayed()
                rule.onNodeWithText("Today").assertIsDisplayed()
                rule.onNodeWithText("Forecast").assertIsDisplayed()
                rule.onNodeWithText("Live · Wi-Fi").assertIsDisplayed()
            }
        }
    }

    @Test
    fun gallery_is_not_in_the_release_manifest() {
        val manifestFile = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml"),
            File("../app/src/main/AndroidManifest.xml")
        ).firstOrNull { it.exists() }
        assertNotNull("src/main/AndroidManifest.xml must exist", manifestFile)
        val content = manifestFile!!.readText()
        assertFalse(
            "ThemeGalleryActivity must not be declared in main AndroidManifest.xml",
            content.contains("ThemeGalleryActivity")
        )
    }
}
