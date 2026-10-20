package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import io.github.khaledbahaaeldin.emberbyte.ui.design.R

/**
 * Roboto Flex expressive typography roles (Contract 13.1).
 *
 * All roles configure the bundled Roboto Flex variable font axes via [FontVariation]:
 * - [hero]: wght 800, wdth 36, opsz 144, letterSpacing -0.04.em
 * - [badge]: wght 850, wdth 110
 * - [title]: wght 800, wdth 40, opsz 144
 * - [heading]: wght 800, wdth 105
 * - [value]: wght 850, wdth 75
 */
object EmberbyteType {
    enum class Role {
        Hero, Badge, Title, Heading, Value
    }

    internal data class TypeAxes(
        val wght: Int,
        val wdth: Float,
        val opsz: Float? = null,
    ) {
        val weight: Int get() = wght
        val width: Float get() = wdth
        val opticalSize: Float? get() = opsz
    }

    internal fun axes(role: Role): TypeAxes = when (role) {
        Role.Hero -> TypeAxes(wght = 800, wdth = 36f, opsz = 144f)
        Role.Badge -> TypeAxes(wght = 850, wdth = 110f, opsz = null)
        Role.Title -> TypeAxes(wght = 800, wdth = 40f, opsz = 144f)
        Role.Heading -> TypeAxes(wght = 800, wdth = 105f, opsz = null)
        Role.Value -> TypeAxes(wght = 850, wdth = 75f, opsz = null)
    }

    internal fun axes(roleName: String): TypeAxes {
        val role = when (roleName.lowercase()) {
            "hero" -> Role.Hero
            "badge" -> Role.Badge
            "title" -> Role.Title
            "heading" -> Role.Heading
            "value" -> Role.Value
            else -> throw IllegalArgumentException("Unknown type role: $roleName")
        }
        return axes(role)
    }

    private fun createFamily(axes: TypeAxes): FontFamily {
        val settings = if (axes.opsz != null) {
            arrayOf(
                FontVariation.weight(axes.wght),
                FontVariation.width(axes.wdth),
                FontVariation.Setting("opsz", axes.opsz),
            )
        } else {
            arrayOf(
                FontVariation.weight(axes.wght),
                FontVariation.width(axes.wdth),
            )
        }
        return FontFamily(
            Font(
                resId = R.font.roboto_flex,
                weight = FontWeight(axes.wght),
                variationSettings = FontVariation.Settings(*settings),
            ),
        )
    }

    private val heroFamily: FontFamily by lazy { createFamily(axes(Role.Hero)) }
    private val badgeFamily: FontFamily by lazy { createFamily(axes(Role.Badge)) }
    private val titleFamily: FontFamily by lazy { createFamily(axes(Role.Title)) }
    private val headingFamily: FontFamily by lazy { createFamily(axes(Role.Heading)) }
    private val valueFamily: FontFamily by lazy { createFamily(axes(Role.Value)) }

    @Suppress("DEPRECATION")
    fun hero(size: TextUnit): TextStyle = TextStyle(
        fontFamily = heroFamily,
        fontSize = size,
        letterSpacing = (-0.04).em,
        lineHeight = size,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
            mode = LineHeightStyle.Mode.Fixed,
        ),
    )

    fun badge(size: TextUnit): TextStyle = TextStyle(
        fontFamily = badgeFamily,
        fontSize = size,
    )

    fun title(size: TextUnit): TextStyle = TextStyle(
        fontFamily = titleFamily,
        fontSize = size,
    )

    fun heading(size: TextUnit): TextStyle = TextStyle(
        fontFamily = headingFamily,
        fontSize = size,
    )

    fun value(size: TextUnit): TextStyle = TextStyle(
        fontFamily = valueFamily,
        fontSize = size,
    )
}
