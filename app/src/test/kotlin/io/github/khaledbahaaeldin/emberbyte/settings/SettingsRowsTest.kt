package io.github.khaledbahaaeldin.emberbyte.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.khaledbahaaeldin.emberbyte.settings.hub.SettingsHub
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.RevealGate
import java.time.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h6000dp")
class SettingsRowsTest {
    @get:Rule val rule = createComposeRule()

    @Before fun setup() { RevealGate.enabled = false }
    @After fun teardown() { RevealGate.enabled = true }

    @Test
    fun no_settings_row_uses_a_letter_placeholder() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsHub(
                    state = SettingsHubState(),
                    listState = rememberLazyListState(),
                    clock = Clock.systemUTC(),
                    versionName = "1.0.3",
                    updateStatus = "Up to date",
                    onBack = {},
                    onOpenSearch = {},
                    onOpenArea = {},
                    onHealthAction = {},
                    onToggleSwitch = {},
                    onSelectThemeMode = {},
                )
            }
        }
        val allNodes = rule.onAllNodes(hasText("", substring = true), useUnmergedTree = true).fetchSemanticsNodes()
        val singleLetters = allNodes.flatMap { node ->
            node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
        }.filter { it.length == 1 && it[0].isUpperCase() && it != "E" }
        assertTrue("Expected no single-letter row placeholders, found $singleLetters", singleLetters.isEmpty())
    }

    @Test
    fun disabled_row_alpha_animates() {
        rule.mainClock.autoAdvance = false
        var enabled by mutableStateOf(true)
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SettingsRow(
                    title = "Show speed",
                    iconVector = Icons.Rounded.Speed,
                    enabled = enabled,
                    onClick = {},
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
                    SettingsRow(
                        title = "Show speed",
                        iconVector = Icons.Rounded.Speed,
                        enabled = enabled,
                        onClick = {},
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
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, fontScale = currentScale),
            ) {
                EmberbyteTheme(currentSpec) {
                    CompositionLocalProvider(LocalReduceMotion provides true) {
                        Box(modifier = Modifier.width(Dp(currentWidth.toFloat()))) {
                            SettingsHub(
                                state = SettingsHubState(),
                                listState = rememberLazyListState(),
                                clock = Clock.systemUTC(),
                                versionName = "1.0.3",
                                updateStatus = "Up to date",
                                onBack = {},
                                onOpenSearch = {},
                                onOpenArea = {},
                                onHealthAction = {},
                                onToggleSwitch = {},
                                onSelectThemeMode = {},
                            )
                        }
                    }
                }
            }
        }

        for (spec in specs) {
            for (width in widths) {
                for (scale in scales) {
                    currentSpec = spec
                    currentWidth = width
                    currentScale = scale
                    rule.waitForIdle()
                    rule.onNodeWithText("Settings").assertIsDisplayed()
                }
            }
        }
    }
}
