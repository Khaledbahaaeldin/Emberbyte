package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdatesRowTest {
    @get:Rule val rule = createComposeRule()

    private fun show(
        source: UpdateSource,
        versionName: String = "1.0.2",
        onOpenUpdater: () -> Unit = {},
        onOpenReleases: () -> Unit = {},
        density: Density? = null,
        direction: LayoutDirection = LayoutDirection.Ltr,
    ) {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides direction,
                    LocalDensity provides (density ?: LocalDensity.current),
                ) {
                    UpdatesRow(
                        updateSource = source,
                        versionName = versionName,
                        onOpenUpdater = onOpenUpdater,
                        onOpenReleases = onOpenReleases,
                    )
                }
            }
        }
    }

    @Test fun unknown_installer_shows_the_github_wording_and_opens_the_releases_page() {
        var releases = 0
        var updater = 0
        show(UpdateSource.Unknown, onOpenUpdater = { updater++ }, onOpenReleases = { releases++ })
        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNode(hasText("You have 1.0.2", substring = true)).assertIsDisplayed()
        rule.onNode(hasText("never goes online", substring = true)).assertIsDisplayed()
        rule.onNodeWithText("Updates").performClick()
        assertEquals(1, releases)
        assertEquals(0, updater)
    }

    @Test fun other_installer_uses_the_same_github_wording() {
        var releases = 0
        show(UpdateSource.Other, onOpenReleases = { releases++ })
        rule.onNode(hasText("You have 1.0.2", substring = true)).assertIsDisplayed()
        rule.onNodeWithText("Updates").performClick()
        assertEquals(1, releases)
    }

    @Test fun obtainium_installer_names_the_updater_and_opens_it() {
        var updater = 0
        var releases = 0
        show(UpdateSource.Obtainium, onOpenUpdater = { updater++ }, onOpenReleases = { releases++ })
        rule.onNodeWithText("Obtainium keeps Emberbyte up to date").assertIsDisplayed()
        rule.onNodeWithText("Updates").performClick()
        assertEquals(1, updater)
        assertEquals(0, releases)
    }

    @Test fun orion_installer_names_its_app() {
        show(UpdateSource.OrionStore)
        rule.onNodeWithText("Orion Store keeps Emberbyte up to date").assertIsDisplayed()
    }

    @Test fun fdroid_installer_names_its_app() {
        show(UpdateSource.FDroid)
        rule.onNodeWithText("F-Droid keeps Emberbyte up to date").assertIsDisplayed()
    }

    @Test fun row_is_at_least_48dp_tall_and_clickable() {
        show(UpdateSource.Unknown)
        rule.onNodeWithText("Updates").assertHasClickAction()
        rule.onNodeWithText("Updates").assertHeightIsAtLeast(48.dp)
    }

    @Test fun row_fits_at_font_scale_2() {
        show(UpdateSource.Unknown, density = Density(1f, fontScale = 2f))
        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNode(hasText("You have 1.0.2", substring = true)).assertIsDisplayed()
    }

    @Test fun known_updater_row_fits_at_font_scale_2() {
        show(UpdateSource.Obtainium, density = Density(1f, fontScale = 2f))
        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Obtainium keeps Emberbyte up to date").assertIsDisplayed()
    }

    @Test fun row_renders_in_rtl() {
        show(UpdateSource.FDroid, direction = LayoutDirection.Rtl)
        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("F-Droid keeps Emberbyte up to date").assertIsDisplayed()
    }
}
