package io.github.khaledbahaaeldin.emberbyte.ui.design.tiles

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.AppRowUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.BarUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.ForecastUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.model.NetworkKindUi
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.Corner
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TilesTest {
    @get:Rule val rule = createComposeRule()

    @Test fun speedTile_shows_rate_and_network() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SpeedTile(rxBps = 4_200_000L, txBps = 350_000L, network = NetworkKindUi.Wifi, units = ByteUnits.DECIMAL)
            }
        }
        rule.onNodeWithText("4.20 MB/s", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Wi-Fi", substring = true).assertIsDisplayed()
    }

    @Test fun speedTile_says_no_connection_when_offline() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                SpeedTile(rxBps = 0L, txBps = 0L, network = null, units = ByteUnits.DECIMAL)
            }
        }
        rule.onNodeWithText("No connection").assertIsDisplayed()
        rule.onNodeWithText("Offline", substring = true).assertIsDisplayed()
    }

    @Test fun forecastTile_shows_headline_and_detail() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ForecastTile(ForecastUi(headline = "Thu", detail = "runs out (±1 day)", confidence = "Medium confidence"))
            }
        }
        rule.onNodeWithText("Thu").assertIsDisplayed()
        rule.onNodeWithText("runs out (±1 day)").assertIsDisplayed()
        rule.onNodeWithText("Medium confidence").assertIsDisplayed()
    }

    @Test fun forecastTile_renders_texts_in_one_node_matching_model_text_under_rtl() {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EmberbyteTheme(EmberbyteThemeSpec.Default) {
                    ForecastTile(ForecastUi(headline = "Mon", detail = "runs out Mon (±1 day)", confidence = "Medium confidence"))
                }
            }
        }
        rule.onNodeWithText("Mon").assertIsDisplayed()
        rule.onNodeWithText("runs out Mon (±1 day)").assertIsDisplayed()
        rule.onNodeWithText("Medium confidence").assertIsDisplayed()
    }

    @Test fun usageBarRow_reports_the_tapped_bar() {
        var picked = -1
        val bars = listOf(
            BarUi("Mon", 1_000_000_000L, "Monday, 1.00 gigabytes"),
            BarUi("Tue", 2_000_000_000L, "Tuesday, 2.00 gigabytes"),
        )
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UsageBarRow(bars, selectedIndex = null, onSelect = { picked = it })
            }
        }
        rule.onNodeWithText("Tue").performClick()
        assertEquals(1, picked)
    }

    @Test fun appRow_shows_name_and_total() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppRow(AppRowUi("com.yt", "YouTube", 610_000_000L, 0L), ByteUnits.DECIMAL, onClick = {})
            }
        }
        rule.onNodeWithText("YouTube").assertIsDisplayed()
        rule.onNodeWithText("610 MB").assertIsDisplayed()
    }

    @Test fun an_app_row_speaks_its_mobile_and_wifi_split() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppRow(AppRowUi("com.video", "Video", 600_000_000L, 400_000_000L), ByteUnits.DECIMAL, onClick = {})
            }
        }
        rule.onNode(hasText("Video"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Mobile 600 megabytes, Wi-Fi 400 megabytes"))
    }

    @Test fun appRow_avatar_letter_is_hidden_from_semantics() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                AppRow(AppRowUi("com.yt", "YouTube", 610_000_000L, 0L), ByteUnits.DECIMAL, onClick = {})
            }
        }
        // "Y" is the avatar initial; clearAndSetSemantics ensures it is decorative and not present in semantics
        rule.onNodeWithText("Y").assertDoesNotExist()
    }

    @Test fun bento_tile_corner_shape_has_one_small_corner() {
        val shape = bentoTileShape(Corner.TopStart) as CornerBasedShape
        val density = Density(1f)
        val size = Size(100f, 100f)
        val outline = shape.createOutline(size, LayoutDirection.Ltr, density) as Outline.Rounded
        assertEquals(8f, outline.roundRect.topLeftCornerRadius.x, 0.01f)
        assertEquals(24f, outline.roundRect.topRightCornerRadius.x, 0.01f)
        assertEquals(24f, outline.roundRect.bottomRightCornerRadius.x, 0.01f)
        assertEquals(24f, outline.roundRect.bottomLeftCornerRadius.x, 0.01f)

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                BentoTile(
                    title = "Test Corner",
                    corner = Corner.TopStart,
                ) {
                    Text("Content")
                }
            }
        }
        rule.onNodeWithText("Test Corner").assertIsDisplayed()
    }

    @Test fun forecast_tile_uses_the_accent_container() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                ForecastTile(ForecastUi(headline = "Thu", detail = "runs out (±1 day)", confidence = "Medium confidence"))
            }
        }
        rule.onNodeWithTag("bento_tile_accent").assertExists()
    }

    @Test fun tile_containers_meet_contrast_requirements() {
        for (themeId in listOf("dusk", "ember", "monochrome", "catppuccin", "gruvbox", "nord", "rosepine", "solarized", "dracula", "tokyonight", "everforest", "kanagawa")) {
            val (dark, light) = io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.GeneratedPalettes[themeId]!!
            for (palette in listOf(dark, light)) {
                org.junit.Assert.assertTrue(
                    "$themeId onAccent / accent",
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio(palette.onAccent, palette.accent) >= 4.5,
                )
                org.junit.Assert.assertTrue(
                    "$themeId onWarn / warn",
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio(palette.onWarn, palette.warn) >= 4.5,
                )
                org.junit.Assert.assertTrue(
                    "$themeId onDanger / danger",
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio(palette.onDanger, palette.danger) >= 4.5,
                )
                org.junit.Assert.assertTrue(
                    "$themeId text on surface2",
                    io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette.contrastRatio(palette.text, palette.surface2) >= 4.5,
                )
            }
        }
    }
}
