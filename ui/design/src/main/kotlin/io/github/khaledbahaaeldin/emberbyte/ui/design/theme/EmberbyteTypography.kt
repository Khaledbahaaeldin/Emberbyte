package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive typography for the app.
 *
 * The regular styles are exactly the Material defaults, so nothing shifts on screens that do not opt in. The
 * `*Emphasized` styles are the Expressive way to stress text: display and headline use the bundled Roboto Flex
 * expressive faces (the same ones as `EmberbyteType`), title, body and label get a heavier weight. 1.0.2 had zero
 * uses of any emphasized style and 23 hand-written `copy(fontWeight = ...)` overrides that these replace.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val EmberbyteTypography: Typography by lazy {
    val base = Typography()
    val displayFamily: FontFamily? = EmberbyteType.title(1.sp).fontFamily
    val headingFamily: FontFamily? = EmberbyteType.heading(1.sp).fontFamily

    fun TextStyle.expressive(family: FontFamily?, weight: FontWeight): TextStyle =
        if (family != null) copy(fontFamily = family, fontWeight = weight) else copy(fontWeight = weight)

    base.copy(
        displayLargeEmphasized = base.displayLarge.expressive(displayFamily, FontWeight.ExtraBold),
        displayMediumEmphasized = base.displayMedium.expressive(displayFamily, FontWeight.ExtraBold),
        displaySmallEmphasized = base.displaySmall.expressive(displayFamily, FontWeight.ExtraBold),
        headlineLargeEmphasized = base.headlineLarge.expressive(headingFamily, FontWeight.ExtraBold),
        headlineMediumEmphasized = base.headlineMedium.expressive(headingFamily, FontWeight.ExtraBold),
        headlineSmallEmphasized = base.headlineSmall.expressive(headingFamily, FontWeight.ExtraBold),
        titleLargeEmphasized = base.titleLarge.expressive(headingFamily, FontWeight.Bold),
        titleMediumEmphasized = base.titleMedium.expressive(null, FontWeight.Bold),
        titleSmallEmphasized = base.titleSmall.expressive(null, FontWeight.Bold),
        bodyLargeEmphasized = base.bodyLarge.expressive(null, FontWeight.SemiBold),
        bodyMediumEmphasized = base.bodyMedium.expressive(null, FontWeight.SemiBold),
        bodySmallEmphasized = base.bodySmall.expressive(null, FontWeight.SemiBold),
        labelLargeEmphasized = base.labelLarge.expressive(null, FontWeight.Bold),
        labelMediumEmphasized = base.labelMedium.expressive(null, FontWeight.Bold),
        labelSmallEmphasized = base.labelSmall.expressive(null, FontWeight.Bold),
    )
}

/** Text sizes of the two Home bento tiles (Space Grotesk). Tokens live here so the tiles carry no raw sizes. */
object BentoType {
    val Label: TextUnit = 14.sp
    val Sub: TextUnit = 12.sp
    val Unit: TextUnit = 15.sp
    val Caption: TextUnit = 14.sp
    val Small: TextUnit = 13.sp
    val BigMax: TextUnit = 60.sp
    val BigMin: TextUnit = 36.sp
    val BigStep: TextUnit = 1.sp
    val SetupTitle: TextUnit = 21.sp
    val SetupBody: TextUnit = 14.sp
    val SetupButton: TextUnit = 17.sp
}

/** Text sizes of the Home usage card (Space Grotesk). Tokens live here so the card carries no raw sizes. */
object UsageType {
    val Title: TextUnit = 22.sp
    val Pill: TextUnit = 13.sp
    val Summary: TextUnit = 15.sp
    val StripValue: TextUnit = 11.sp
    val StripValueSelected: TextUnit = 12.sp
    val DiscLetter: TextUnit = 12.sp
    val FeaturedName: TextUnit = 21.sp
    val FeaturedAmount: TextUnit = 30.sp
    val FeaturedSub: TextUnit = 13.sp
    val RowName: TextUnit = 17.sp
    val RowAmount: TextUnit = 21.sp
    val RowSub: TextUnit = 12.sp
    val Button: TextUnit = 15.sp
    val Chip: TextUnit = 13.sp
}
