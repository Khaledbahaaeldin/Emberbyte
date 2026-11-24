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

/** Text sizes of the Apps page (Space Grotesk). Tokens live here so the page carries no raw sizes. */
object AppsType {
    val Title: TextUnit = 34.sp
    val Pill: TextUnit = 13.sp
    val Search: TextUnit = 16.sp
    val Badge: TextUnit = 11.sp
    val Period: TextUnit = 14.sp
    val Chip: TextUnit = 13.sp
    val SummaryLabel: TextUnit = 14.sp
    val SummaryTotal: TextUnit = 48.sp
    val SummaryUnit: TextUnit = 20.sp
    val SummarySplit: TextUnit = 13.sp
    val MostCaption: TextUnit = 11.sp
    val MostName: TextUnit = 22.sp
    val MostAmount: TextUnit = 30.sp
    val MostSub: TextUnit = 13.sp
    val RowName: TextUnit = 17.sp
    val RowAmount: TextUnit = 20.sp
    val RowSub: TextUnit = 12.sp
    val ListHeader: TextUnit = 14.sp
    val SheetTitle: TextUnit = 22.sp
    val SheetLabel: TextUnit = 14.sp
    val SheetButton: TextUnit = 15.sp
}

/** Text sizes of the App detail screen (Space Grotesk). Tokens live here so the screen carries no raw sizes. */
object DetailType {
    val Title: TextUnit = 28.sp
    val Package: TextUnit = 13.sp
    val Pill: TextUnit = 13.sp
    val Label: TextUnit = 14.sp
    val Total: TextUnit = 64.sp
    val TotalUnit: TextUnit = 24.sp
    val Change: TextUnit = 14.sp
    val Sentence: TextUnit = 16.sp
    val CardTitle: TextUnit = 16.sp
    val Small: TextUnit = 12.sp
    val TileValue: TextUnit = 26.sp
    val TileSub: TextUnit = 12.sp
    val PartPercent: TextUnit = 17.sp
    val Button: TextUnit = 15.sp
}

/** Text sizes of the History screen sections (Space Grotesk). Tokens live here so the screen carries no raw sizes. */
object HistoryType {
    val CardTitle: TextUnit = 16.sp
    val Sentence: TextUnit = 16.sp
    val Label: TextUnit = 14.sp
    val Small: TextUnit = 12.sp
    val TileValue: TextUnit = 26.sp
    val SelectedTotal: TextUnit = 40.sp
    val Record: TextUnit = 20.sp
}

/** Text sizes for shared `ui/design` components. Tokens live here so components carry no raw `.sp` literals. */
object ComponentType {
    val DialogTitle: TextUnit = 24.sp
    val DialogBody: TextUnit = 15.sp
    val NavLabel: TextUnit = 13.sp
    val NavLabelCharWidth: TextUnit = 7.2f.sp
    val HeroMax: TextUnit = 96.sp
    val HeroMin: TextUnit = 36.sp
    val MorphingSize: TextUnit = 72.sp
    val RingLabel: TextUnit = 17.sp
    val EmptyTitle: TextUnit = 20.sp
    val RowValue: TextUnit = 12.sp
}

/** Text sizes for the Licences screen. Tokens live here so the screen carries no raw `.sp` literals. */
object LicencesType {
    val Header: TextUnit = 18.sp
    val SectionTracking: TextUnit = 1.sp
    val ItemTitle: TextUnit = 15.sp
    val LicenceBody: TextUnit = 11.sp
    val LicenceLineHeight: TextUnit = 15.sp
}

/** Text sizes for Onboarding screens. Tokens live here so onboarding carries no raw `.sp` literals. */
object OnboardingType {
    val HeroTitle: TextUnit = 16.sp
    val Body: TextUnit = 14.sp
    val HeroValue: TextUnit = 76.sp
    val Action: TextUnit = 15.sp
    val Button: TextUnit = 16.sp
}

/** Text sizes for home-screen Glance widgets. Tokens live here so widgets carry no raw `.sp` literals. */
object WidgetType {
    val HeroLarge: TextUnit = 38.sp
    val HeroToday: TextUnit = 32.sp
    val HeroCompact: TextUnit = 28.sp
    val Metric: TextUnit = 24.sp
    val Unit: TextUnit = 16.sp
    val Title: TextUnit = 15.sp
    val UnitSmall: TextUnit = 14.sp
    val Body: TextUnit = 13.sp
    val Label: TextUnit = 12.sp
    val Caption: TextUnit = 11.sp
}

/** Text sizes for the redesigned Settings hub and sub-screens. */
object SettingsType {
    val HubTitle: TextUnit = 38.sp
    val BannerTitle: TextUnit = 34.sp
    val UnitsDisplay: TextUnit = 44.sp
    val PulseHeadline: TextUnit = 23.sp
    val CardTitle: TextUnit = 17.sp
    val RowTitle: TextUnit = 15.sp
    val PulseBody: TextUnit = 13.sp
    val RowSubtitle: TextUnit = 12.5f.sp
    val Caption: TextUnit = 12.sp
    val Micro: TextUnit = 11.sp
}
