package io.github.khaledbahaaeldin.emberbyte.apps

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.khaledbahaaeldin.emberbyte.home.SpikeTileUi
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
class SpikeBannerTest {
    @get:Rule
    val rule = createComposeRule()

    private val spike = SpikeTileUi(42L, "com.video", "Video used a lot of data", "1.24 GB in two hours, 8.3x its usual")

    @Test
    fun the_banner_opens_the_app_and_dismisses_by_id() {
        var opened: String? = null
        var dismissed: Long? = null
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SpikeBanner(spike, onOpen = { opened = spike.packageName }, onDismiss = { dismissed = spike.id })
            }
        }
        rule.onNodeWithText("Video used a lot of data").performClick()
        assertEquals("com.video", opened)
        rule.onNodeWithText("Dismiss").performClick()
        assertEquals(42L, dismissed)
    }
}
