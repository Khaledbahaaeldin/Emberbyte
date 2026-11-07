package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdatesRowTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun row_shows_github_releases_when_unknown_and_opens_releases() {
        var openedReleases = false
        var openedUpdater = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.Unknown,
                    onOpenReleases = { openedReleases = true },
                    onOpenUpdater = { openedUpdater = true },
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("GitHub Releases").assertIsDisplayed()
        rule.onNode(hasText("Updates") and hasClickAction()).performClick()

        assertTrue(openedReleases)
        assertFalse(openedUpdater)
    }

    @Test
    fun row_shows_github_releases_when_other_and_opens_releases() {
        var openedReleases = false
        var openedUpdater = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.Other,
                    onOpenReleases = { openedReleases = true },
                    onOpenUpdater = { openedUpdater = true },
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("GitHub Releases").assertIsDisplayed()
        rule.onNode(hasText("Updates") and hasClickAction()).performClick()

        assertTrue(openedReleases)
        assertFalse(openedUpdater)
    }

    @Test
    fun row_shows_managed_by_obtainium_and_opens_updater() {
        var openedReleases = false
        var openedUpdater = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.Obtainium,
                    onOpenReleases = { openedReleases = true },
                    onOpenUpdater = { openedUpdater = true },
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Managed by Obtainium").assertIsDisplayed()
        rule.onNode(hasText("Updates") and hasClickAction()).performClick()

        assertTrue(openedUpdater)
        assertFalse(openedReleases)
    }

    @Test
    fun row_shows_managed_by_orion_store_and_opens_updater() {
        var openedReleases = false
        var openedUpdater = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.OrionStore,
                    onOpenReleases = { openedReleases = true },
                    onOpenUpdater = { openedUpdater = true },
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Managed by Orion Store").assertIsDisplayed()
        rule.onNode(hasText("Updates") and hasClickAction()).performClick()

        assertTrue(openedUpdater)
        assertFalse(openedReleases)
    }

    @Test
    fun row_shows_managed_by_fdroid_and_opens_updater() {
        var openedReleases = false
        var openedUpdater = false

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.FDroid,
                    onOpenReleases = { openedReleases = true },
                    onOpenUpdater = { openedUpdater = true },
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Managed by F-Droid").assertIsDisplayed()
        rule.onNode(hasText("Updates") and hasClickAction()).performClick()

        assertTrue(openedUpdater)
        assertFalse(openedReleases)
    }

    @Test
    fun known_updater_without_click_action_is_not_clickable() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.Obtainium,
                    onOpenReleases = {},
                    onOpenUpdater = null,
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Managed by Obtainium").assertIsDisplayed()
        rule.onNode(hasText("Updates") and hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun font_scale_2_no_clip() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    UpdatesRow(
                        updateSource = UpdateSource.Obtainium,
                        onOpenUpdater = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Managed by Obtainium").assertIsDisplayed()
    }

    @Test
    fun rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    UpdatesRow(
                        updateSource = UpdateSource.FDroid,
                        onOpenUpdater = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNodeWithText("Managed by F-Droid").assertIsDisplayed()
    }
}
