package io.github.khaledbahaaeldin.emberbyte.plans

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.engine.model.FirstCycle
import io.github.khaledbahaaeldin.emberbyte.plans.flow.FirstCycleChoice
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlanEditorScreenTest {
    @get:Rule val rule = createComposeRule()

    private val sampleState = PlanEditorUiState(
        isNew = false,
        loading = false,
        planId = 1L,
        form = PlanEditorForm(
            name = "Test Plan",
            capText = "10",
            capUnit = CapUnit.GB,
            firstCycle = FirstCycle.Entered(
                usedBytes = 2_000_000_000L,
                enteredAt = Instant.parse("2026-10-15T12:00:00Z"),
                cycleStart = Instant.parse("2026-10-01T00:00:00Z"),
            ),
            firstCycleChoice = FirstCycleChoice.Entered,
            enteredAmount = "2",
            enteredUnit = CapUnit.GB,
        ),
    )

    @Test
    fun this_cycle_section_shows_the_current_mode() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = sampleState,
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        rule.onNodeWithText("This cycle").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("I've used some already").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun delete_still_requires_confirmation() {
        var deleteCalled = false
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = sampleState,
                    onEvent = { event ->
                        if (event is PlanEditorEvent.Delete) {
                            deleteCalled = true
                        }
                    },
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        rule.onNodeWithText("Delete plan").performScrollTo().assertIsDisplayed().performClick()
        rule.onNodeWithText("Delete plan?").assertIsDisplayed()
        assertEquals(false, deleteCalled)
        rule.onNodeWithText("Delete").assertIsDisplayed().performClick()
        assertEquals(true, deleteCalled)
    }

    @Test
    fun sections_have_headings_for_talkback_navigation() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                PlanEditorContent(
                    state = sampleState,
                    onEvent = {},
                    onBack = {},
                    onPrompt = {},
                )
            }
        }
        val expectedHeadings = listOf("Basics", "Cap and cycle", "This cycle", "Alerts", "Danger zone")
        for (heading in expectedHeadings) {
            val node = rule.onNode(
                SemanticsMatcher.expectValue(SemanticsProperties.Text, listOf(androidx.compose.ui.text.AnnotatedString(heading)))
                    .and(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)),
            )
            node.performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun font_scale_2_no_clip() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(2.0f, fontScale = 2.0f)) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    Box(modifier = Modifier.width(360.dp)) {
                        PlanEditorContent(
                            state = sampleState,
                            onEvent = {},
                            onBack = {},
                            onPrompt = {},
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("Save").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    PlanEditorContent(
                        state = sampleState,
                        onEvent = {},
                        onBack = {},
                        onPrompt = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Save").performScrollTo().assertIsDisplayed()
    }
}
