package io.github.khaledbahaaeldin.emberbyte.tokens

/**
 * Files that still contain raw corner radii or raw text-size literals, with how many sites each has today.
 *
 * T12b (the migration sweep) moves every site onto the shape scale and the typography roles and shrinks these maps
 * until they are empty. The scan tests fail when a file has MORE sites than listed (a new raw radius or size was
 * added) and when a listed file no longer exists. Never add an entry to make a test pass: use the tokens instead.
 */
internal object TokenAllowLists {
    val rawRadii: Map<String, Int> = mapOf(
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/apps/AppsScreen.kt" to 2,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/apps/FeaturedAppCard.kt" to 1,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/nav/BackTransitions.kt" to 1,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/nav/GuardedBack.kt" to 1,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/placeholder/StartupPlaceholder.kt" to 4,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/plans/flow/PlanFlowScreen.kt" to 1,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/AppearanceScreen.kt" to 1,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/LicencesScreen.kt" to 4,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/SettingsGroups.kt" to 2,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/SettingsScreen.kt" to 7,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/tiles/BentoTile.kt" to 1,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/tiles/NoticeLine.kt" to 1,
    )

    val rawSizes: Map<String, Int> = mapOf(
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/apps/AppDetail.kt" to 3,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/onboarding/OnboardingPages.kt" to 5,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/onboarding/OnboardingScreen.kt" to 1,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/LicencesScreen.kt" to 7,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/SettingsGroups.kt" to 4,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/settings/SettingsScreen.kt" to 3,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/widget/DashboardWidget.kt" to 7,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/widget/PlanWidget.kt" to 7,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/widget/TodayWidget.kt" to 2,
        "app/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/widget/WidgetBlocks.kt" to 14,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/dialog/EmberbyteDialog.kt" to 3,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/navbar/CompactNavBar.kt" to 1,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/number/HeroNumber.kt" to 3,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/number/MorphingNumber.kt" to 2,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/progress/WavyRing.kt" to 1,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/states/EmptyState.kt" to 1,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/tiles/AppRow.kt" to 1,
        "ui/design/src/main/kotlin/io/github/khaledbahaaeldin/emberbyte/ui/design/tiles/UsageBarRow.kt" to 1,
    )
}
