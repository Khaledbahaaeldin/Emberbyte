package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.data.Settings
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class SettingsRowsTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun no_settings_row_uses_a_letter_placeholder() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsContent(
                    settings = Settings(),
                    onUpdate = {},
                    onBack = {},
                    versionName = "1.0.2",
                    onOpenSource = {},
                    onOpenBatterySettings = {},
                    dataUiState = DataUiState(),
                )
            }
        }

        // Single-letter placeholder tiles ("A", "B", "C", "J", "R") must not exist anywhere in Settings.
        val allNodes = rule.onAllNodes(hasText("", substring = true), useUnmergedTree = true).fetchSemanticsNodes()
        val singleLetters = allNodes.flatMap { node ->
            node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
        }.filter { it.length == 1 && it[0].isUpperCase() }
        assertTrue("Expected no single-letter row placeholders, found $singleLetters", singleLetters.isEmpty())

        // Every leading tile in Settings must render a real vector icon.
        val iconTiles = rule.onAllNodesWithTag("settings_row_icon_vector", useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Expected vector icons on all settings rows, found ${iconTiles.size}", iconTiles.size >= 10)
    }

    @Test
    fun settings_rows_share_a_48dp_leading_column() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsContent(
                    settings = Settings(),
                    onUpdate = {},
                    onBack = {},
                    versionName = "1.0.2",
                    onOpenSource = {},
                    onOpenBatterySettings = {},
                    dataUiState = DataUiState(),
                )
            }
        }

        val titles = listOf(
            "Live notification",
            "Live updates",
            "Show speed",
            "Spike alerts",
            "Export usage (CSV)",
            "Export everything (JSON)",
            "Create backup",
            "Restore backup",
            "Updates",
        )
        val xPositions = titles.map { label ->
            label to rule.onNodeWithText(label, useUnmergedTree = true).fetchSemanticsNode().positionInRoot.x
        }
        val firstX = xPositions.first().second
        for ((label, x) in xPositions) {
            assertEquals("Row title '$label' X column must align with '$firstX'", firstX, x, 1.5f)
        }
    }

    @Test
    fun updates_row_is_single_line_with_external_link_and_browser_subtitle() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UpdatesRow(
                    updateSource = UpdateSource.Unknown,
                    versionName = "1.0.2",
                )
            }
        }

        rule.onNodeWithText("Updates").assertIsDisplayed()
        rule.onNode(hasText("Opens your browser", substring = true)).assertIsDisplayed()
        rule.onNode(hasTestTag("settings_row_external_link"), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun disabled_row_alpha_animates() {
        rule.mainClock.autoAdvance = false
        var enabled by mutableStateOf(true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsContent(
                    settings = Settings(liveNotificationEnabled = enabled),
                    onUpdate = {},
                    onBack = {},
                    versionName = "1.0.2",
                    onOpenSource = {},
                    onOpenBatterySettings = {},
                )
            }
        }
        rule.mainClock.advanceTimeBy(500)

        enabled = false
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(48)

        val midNode = rule.onNode(hasTestTag("settings_row_Show speed")).fetchSemanticsNode()
        val midAlpha = midNode.config.firstOrNull { it.key.name == "SettingsRowAlpha" }?.value as? Float
        assertTrue("Expected animated in-between row alpha between 0.38 and 1.0, got $midAlpha", midAlpha != null && midAlpha > 0.38f && midAlpha < 1.0f)
    }

    @Test
    fun disabled_row_alpha_snaps_when_reduced_motion() {
        rule.mainClock.autoAdvance = false
        var enabled by mutableStateOf(true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    SettingsContent(
                        settings = Settings(liveNotificationEnabled = enabled),
                        onUpdate = {},
                        onBack = {},
                        versionName = "1.0.2",
                        onOpenSource = {},
                        onOpenBatterySettings = {},
                    )
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()

        enabled = false
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()

        val snappedNode = rule.onNode(hasTestTag("settings_row_Show speed")).fetchSemanticsNode()
        val snappedAlpha = snappedNode.config.firstOrNull { it.key.name == "SettingsRowAlpha" }?.value as? Float
        assertEquals(0.38f, snappedAlpha ?: -1f, 0.005f)
    }

    @Test
    fun settings_and_dialogs_render_cleanly_at_360_411dp_and_font_scales_1_to_2_in_light_dark_amoled() {
        val specs = listOf(
            EmberbyteThemeSpec(presetId = "ember", darkTheme = false, amoled = false),
            EmberbyteThemeSpec(presetId = "ember", darkTheme = true, amoled = false),
            EmberbyteThemeSpec(presetId = "ember", darkTheme = true, amoled = true),
        )
        val widths = listOf(360, 411)
        val scales = listOf(1.0f, 1.5f, 2.0f)

        var currentSpec by mutableStateOf(specs.first())
        var currentWidth by mutableStateOf(411)
        var currentScale by mutableStateOf(1.0f)

        rule.setContent {
            val base = androidx.compose.ui.platform.LocalDensity.current
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(base.density, fontScale = currentScale),
            ) {
                EmberbyteTheme(currentSpec) {
                    CompositionLocalProvider(LocalReduceMotion provides true) {
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier.width(
                                androidx.compose.ui.unit.Dp(currentWidth.toFloat()),
                            ),
                        ) {
                            SettingsContent(
                                settings = Settings(themeId = "ember"),
                                onUpdate = {},
                                onBack = {},
                                versionName = "1.0.2",
                                onOpenSource = {},
                                onOpenBatterySettings = {},
                            )
                        }
                    }
                }
            }
        }

        for (spec in specs) {
            currentSpec = spec
            for (w in widths) {
                currentWidth = w
                for (scale in scales) {
                    currentScale = scale
                    rule.waitForIdle()
                    val minPx = with(rule.density) { 47.5f * density }
                    val maxWidthPx = with(rule.density) { (w + 1) * density }
                    val clickables = rule.onAllNodes(androidx.compose.ui.test.hasClickAction()).fetchSemanticsNodes()
                    assertTrue(clickables.isNotEmpty())
                    for (node in clickables) {
                        val b = node.boundsInRoot
                        assertTrue("Touch target height ${b.height}px < ${minPx}px at width=$w scale=$scale", b.height >= minPx)
                        assertTrue("Control overflows ${w}dp container (${b.right} > $maxWidthPx) at scale=$scale", b.right <= maxWidthPx)
                    }
                }
            }
        }
    }
}
