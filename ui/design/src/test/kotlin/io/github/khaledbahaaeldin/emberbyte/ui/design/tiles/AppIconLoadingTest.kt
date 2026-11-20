package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import android.graphics.Bitmap
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.motion.EmberbyteCrossfade
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.states.LoadingState
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppIconLoadingTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun icon_crossfades_from_placeholder() {
        rule.mainClock.autoAdvance = false
        val iconState = mutableStateOf<ImageBitmap?>(null)

        rule.setContent {
            EmberbyteTheme {
                AppIcon(
                    label = "TestApp",
                    icon = iconState.value,
                    shape = AppShape.Cookie,
                    size = 40.dp,
                )
            }
        }

        // Initially shows letter placeholder
        rule.onNodeWithText("T", useUnmergedTree = true).assertExists()

        // Provide bitmap
        iconState.value = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).asImageBitmap()
        rule.mainClock.advanceTimeByFrame()

        // Advance 30ms: Crossfade is in flight, placeholder still partially visible in composition
        rule.mainClock.advanceTimeBy(30)
        rule.onNodeWithText("T", useUnmergedTree = true).assertExists()

        // Advance to completion: placeholder gone
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("T", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun reduced_motion_loading_swaps_without_fade() {
        rule.mainClock.autoAdvance = false
        val loading = mutableStateOf(true)

        rule.setContent {
            CompositionLocalProvider(LocalReduceMotion provides true) {
                EmberbyteTheme {
                    EmberbyteCrossfade(targetState = loading.value) { isLoading ->
                        if (isLoading) {
                            LoadingState(label = "Loading")
                        } else {
                            Text("Content Ready")
                        }
                    }
                }
            }
        }

        rule.onNodeWithText("Content Ready").assertDoesNotExist()

        loading.value = false
        rule.mainClock.advanceTimeByFrame()

        // Snaps immediately without fade frames
        rule.onNodeWithText("Content Ready").assertExists()
    }
}
