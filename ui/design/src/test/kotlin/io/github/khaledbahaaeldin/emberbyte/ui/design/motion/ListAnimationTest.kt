package io.github.khaledbahaaeldin.emberbyte.ui.design.motion

import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ListAnimationTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun removing_an_item_keeps_the_others_and_does_not_crash() {
        var itemsList by mutableStateOf(listOf("Item 1", "Item 2", "Item 3"))

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                val motion = rememberEmberbyteMotion()
                LazyColumn {
                    items(itemsList, key = { it }) { item ->
                        Text(
                            text = item,
                            modifier = Modifier
                                .testTag(item)
                                .animateItem(
                                    fadeInSpec = motion.effects(),
                                    placementSpec = motion.spatial(),
                                    fadeOutSpec = motion.effects(),
                                )
                        )
                    }
                }
            }
        }

        rule.onNodeWithTag("Item 1").assertIsDisplayed()
        rule.onNodeWithTag("Item 2").assertIsDisplayed()
        rule.onNodeWithTag("Item 3").assertIsDisplayed()

        // Remove "Item 2"
        itemsList = listOf("Item 1", "Item 3")
        rule.waitForIdle()

        rule.onNodeWithTag("Item 1").assertIsDisplayed()
        rule.onNodeWithTag("Item 3").assertIsDisplayed()
        rule.onNodeWithTag("Item 2").assertDoesNotExist()
    }

    @Test
    fun reduced_motion_lists_use_snap_specs() {
        var itemsList by mutableStateOf(listOf("Alpha", "Beta", "Gamma"))
        var capturedMotion: EmberbyteMotion? = null

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(LocalReduceMotion provides true) {
                    val motion = rememberEmberbyteMotion()
                    capturedMotion = motion
                    LazyColumn {
                        items(itemsList, key = { it }) { item ->
                            Text(
                                text = item,
                                modifier = Modifier
                                    .testTag(item)
                                    .animateItem(
                                        fadeInSpec = motion.effects(),
                                        placementSpec = motion.spatial(),
                                        fadeOutSpec = motion.effects(),
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Verify motion specs are snap for spatial and 90ms tween for effects under reduced motion
        val motion = capturedMotion ?: error("Motion specs not captured")
        assertEquals(snap<androidx.compose.ui.unit.IntOffset>(), motion.spatial<androidx.compose.ui.unit.IntOffset>())
        assertEquals(tween<Float>(REDUCED_MOTION_EFFECTS_DURATION_MS), motion.effects<Float>())

        rule.onNodeWithText("Alpha").assertIsDisplayed()
        rule.onNodeWithText("Beta").assertIsDisplayed()
        rule.onNodeWithText("Gamma").assertIsDisplayed()

        // Remove Alpha under reduced motion
        itemsList = listOf("Beta", "Gamma")
        rule.waitForIdle()

        rule.onNodeWithText("Beta").assertIsDisplayed()
        rule.onNodeWithText("Gamma").assertIsDisplayed()
        rule.onNodeWithText("Alpha").assertDoesNotExist()
    }
}
