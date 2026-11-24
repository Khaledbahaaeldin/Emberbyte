package io.github.khaledbahaaeldin.emberbyte.whatsnew

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.settings.DataUiState
import io.github.khaledbahaaeldin.emberbyte.settings.SettingsContent
import io.github.khaledbahaaeldin.emberbyte.settings.UpdateSource
import io.github.khaledbahaaeldin.emberbyte.settings.UpdatesRow
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class WhatsNewSheetTest {
    @get:Rule val rule = createComposeRule()

    private val currentEntry = WhatsNewEntry(
        versionCode = 4,
        versionName = "1.0.3",
        date = LocalDate.of(2026, 11, 24),
        highlights = listOf(
            Highlight(Kind.Added, "What's new sheet built offline from app assets."),
            Highlight(Kind.Changed, "Settings rows use consistent icons."),
            Highlight(Kind.Fixed, "Fixed plan editor scroll and Save bar."),
        ),
    )

    private val olderEntry = WhatsNewEntry(
        versionCode = 3,
        versionName = "1.0.2",
        date = LocalDate.of(2026, 11, 12),
        highlights = listOf(
            Highlight(Kind.Added, "Updates row in Settings."),
        ),
    )

    @Test
    fun sheet_shows_title_date_and_three_sections() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WhatsNewSheet(
                    entry = currentEntry,
                    olderEntries = listOf(olderEntry),
                    onDismiss = {},
                    onOpenChangelog = {},
                )
            }
        }

        rule.onNodeWithText("What's new in 1.0.3").assertIsDisplayed()
        rule.onNodeWithText("24 Nov 2026").assertIsDisplayed()
        rule.onNodeWithText("No account, no internet, still just your data.").assertIsDisplayed()
        rule.onNodeWithContentDescription("Added, 1 items").assertIsDisplayed()
        rule.onNodeWithContentDescription("Changed, 1 items").assertIsDisplayed()
        rule.onNodeWithContentDescription("Fixed, 1 items").assertIsDisplayed()
        rule.onNodeWithText("What's new sheet built offline from app assets.").assertIsDisplayed()
        rule.onNodeWithText("Got it").assertHeightIsAtLeast(48.dp)
        rule.onNodeWithText("See all changes").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun got_it_dismisses_and_marks_seen() {
        var dismissed = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WhatsNewSheet(
                    entry = currentEntry,
                    olderEntries = emptyList(),
                    onDismiss = { dismissed++ },
                    onOpenChangelog = {},
                )
            }
        }

        rule.onNodeWithText("Got it").performClick()
        assertEquals(1, dismissed)
    }

    @Test
    fun swipe_dismiss_marks_seen() {
        var dismissed = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WhatsNewSheet(
                    entry = currentEntry,
                    olderEntries = emptyList(),
                    onDismiss = { dismissed++ },
                    onOpenChangelog = {},
                )
            }
        }

        rule.onNodeWithText("What's new in 1.0.3").performTouchInput { swipeDown() }
        rule.waitForIdle()
        assertTrue("Swiping down on the sheet should trigger onDismiss", dismissed >= 1)
    }

    @Test
    fun see_all_changes_opens_the_changelog_url() {
        var opened = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WhatsNewSheet(
                    entry = currentEntry,
                    olderEntries = emptyList(),
                    onDismiss = {},
                    onOpenChangelog = { opened++ },
                )
            }
        }

        rule.onNodeWithText("See all changes").performClick()
        assertEquals(1, opened)
    }

    @Test
    fun earlier_versions_expand_and_collapse() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                WhatsNewSheet(
                    entry = currentEntry,
                    olderEntries = listOf(olderEntry),
                    onDismiss = {},
                    onOpenChangelog = {},
                )
            }
        }

        rule.onNodeWithText("Earlier versions").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Updates row in Settings.").assertDoesNotExist()
        rule.onNodeWithText("1.0.2").performScrollTo().performClick()
        rule.onNodeWithText("Updates row in Settings.").assertIsDisplayed()
        rule.onNodeWithText("1.0.2").performClick()
        rule.onNodeWithText("Updates row in Settings.").assertDoesNotExist()
    }

    @Test
    fun sheet_scrolls_at_font_scale_2() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalDensity provides Density(2f, fontScale = 2f)) {
                    WhatsNewSheet(
                        entry = currentEntry,
                        olderEntries = listOf(olderEntry),
                        onDismiss = {},
                        onOpenChangelog = {},
                    )
                }
            }
        }

        rule.onNodeWithText("What's new in 1.0.3").assertIsDisplayed()
        rule.onNodeWithText("Got it").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reduced_motion_shows_content_without_stagger() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    WhatsNewSheetContent(
                        entry = currentEntry,
                        olderEntries = emptyList(),
                        onDismiss = {},
                        onOpenChangelog = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("What's new sheet built offline from app assets.").assertIsDisplayed()
        rule.onNodeWithText("Settings rows use consistent icons.").assertIsDisplayed()
        rule.onNodeWithText("Fixed plan editor scroll and Save bar.").assertIsDisplayed()
    }

    @Test
    fun rtl_renders() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WhatsNewSheetContent(
                        entry = currentEntry,
                        olderEntries = emptyList(),
                        onDismiss = {},
                        onOpenChangelog = {},
                    )
                }
            }
        }

        rule.onNodeWithText("What's new in 1.0.3").assertIsDisplayed()
        rule.onNodeWithText("Got it").assertIsDisplayed()
    }

    @Test
    fun settings_about_row_opens_the_sheet() {
        var opened = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsContent(
                    settings = io.github.khaledbahaaeldin.emberbyte.data.Settings(),
                    onUpdate = {},
                    onBack = {},
                    versionName = "1.0.2",
                    onOpenSource = {},
                    onOpenBatterySettings = {},
                    dataUiState = DataUiState(),
                    onOpenWhatsNew = { opened++ },
                    hasWhatsNew = true,
                )
            }
        }

        rule.onNodeWithText("What's new").assertIsDisplayed().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun updates_row_link_opens_the_sheet() {
        var opened = 0
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.Unknown,
                    versionName = "1.0.2",
                    onOpenWhatsNew = { opened++ },
                )
            }
        }

        rule.onNodeWithText("See what's new").assertIsDisplayed().performClick()
        assertEquals(1, opened)
    }
}
