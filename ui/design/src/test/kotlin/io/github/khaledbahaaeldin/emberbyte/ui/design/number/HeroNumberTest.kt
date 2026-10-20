package io.github.khaledbahaaeldin.emberbyte.ui.design.number

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledbahaaeldin.emberbyte.ui.design.format.ByteUnits
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteTheme
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteThemeSpec
import io.github.khaledbahaaeldin.emberbyte.ui.design.theme.LocalReduceMotion
import io.github.khaledbahaaeldin.emberbyte.ui.design.tiles.UnitBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroNumberTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun decimal_point_loosens_spacing_after_dot_and_digits_do_not_intersect() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                val density = LocalDensity.current
                val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

                val withoutFix = textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString("1.48"),
                    style = io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType.hero(96.sp),
                    density = density,
                )
                val withFix = textMeasurer.measure(
                    text = formatHeroValue("1.48"),
                    style = io.github.khaledbahaaeldin.emberbyte.ui.design.theme.EmberbyteType.hero(96.sp),
                    density = density,
                )

                val dotBounds = withFix.getBoundingBox(1) // '.' is index 1
                val nextDigitBounds = withFix.getBoundingBox(2) // '4' is index 2
                assertTrue(
                    "Rendered width with fix (${withFix.size.width}) must be larger than without (${withoutFix.size.width})",
                    withFix.size.width > withoutFix.size.width
                )
                assertTrue(
                    "Dot bounds right (${dotBounds.right}) must not exceed next digit left (${nextDigitBounds.left})",
                    dotBounds.right <= nextDigitBounds.left
                )
            }
        }
    }

    @Test
    fun exposes_one_node_with_the_description() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                HeroNumber(
                    value = "1.38",
                    unit = "GB",
                    contentDescription = "1.38 gigabytes used today, 12.4 gigabytes left",
                )
            }
        }

        // Only ONE node with the full description should be exposed
        rule.onNodeWithContentDescription("1.38 gigabytes used today, 12.4 gigabytes left")
            .assertIsDisplayed()

        // Unmerged semantics tree must not expose the raw value or badge text as separate nodes
        rule.onAllNodesWithText("1.38", useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText("GB", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun fits_at_font_scale_2_in_a_220_dp_container() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalDensity provides Density(density = 2.75f, fontScale = 2.0f),
                ) {
                    Box(modifier = Modifier.width(220.dp)) {
                        HeroNumber(
                            value = "1.38",
                            unit = "GB",
                            contentDescription = "1.38 gigabytes used today",
                        )
                    }
                }
            }
        }

        val node = rule.onNodeWithContentDescription("1.38 gigabytes used today")
        node.assertIsDisplayed()
        val bounds = node.getUnclippedBoundsInRoot()
        val nodeWidth = bounds.right - bounds.left
        assertTrue(
            "Hero number width $nodeWidth must fit in 220.dp container",
            nodeWidth <= 220.dp + 1.dp,
        )
    }

    @Test
    fun shrinks_but_never_below_36sp() {
        var resolvedSizeTiny: androidx.compose.ui.unit.TextUnit? = null
        var resolvedSizeNormal: androidx.compose.ui.unit.TextUnit? = null

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                // In a tiny container where even 36sp doesn't fit
                Box(modifier = Modifier.width(40.dp)) {
                    HeroNumber(
                        value = "999.99",
                        unit = "GB",
                        contentDescription = "Tiny container number",
                        size = 96.sp,
                        onSizeResolved = { resolvedSizeTiny = it },
                    )
                }

                // In a large unconstrained container
                Box(modifier = Modifier.width(600.dp)) {
                    HeroNumber(
                        value = "1.2",
                        unit = "GB",
                        contentDescription = "Large container number",
                        size = 96.sp,
                        onSizeResolved = { resolvedSizeNormal = it },
                    )
                }
            }
        }

        assertEquals("Should not shrink below 36.sp even in tiny container", 36.sp, resolvedSizeTiny)
        assertEquals("Should stay at target 96.sp when width is ample", 96.sp, resolvedSizeNormal)
    }

    @Test
    fun unit_badge_is_hidden_from_semantics() {
        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                UnitBadge(unit = "MB")
            }
        }

        // UnitBadge must be hidden from semantics
        rule.onAllNodesWithText("MB", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun reduce_motion_snaps_the_morph() {
        var capturedLive: Boolean? = null
        var capturedSpec: androidx.compose.animation.core.AnimationSpec<Float>? = null

        rule.setContent {
            EmberbyteTheme(EmberbyteThemeSpec.Default) {
                CompositionLocalProvider(
                    LocalReduceMotion provides true,
                ) {
                    MorphingNumber(
                        bytes = 1_240_000_000L,
                        throughputBps = 20_000_000L,
                        units = ByteUnits.DECIMAL,
                        contentDescription = "1.24 gigabytes used today",
                        animate = true,
                        onMotionResolved = { live, spec ->
                            capturedLive = live
                            capturedSpec = spec
                        },
                    )
                }
            }
        }

        assertEquals(false, capturedLive)
        assertTrue(
            "Intensity animation spec must be SnapSpec when reduce motion is enabled",
            capturedSpec is androidx.compose.animation.core.SnapSpec,
        )
    }
}
