package io.github.khaledbahaaeldin.emberbyte.ui.design.theme.palette

data class ThemePreset(
    val id: String,
    val name: String,
    val dark: EmberbytePalette,
    val light: EmberbytePalette,
)

val ThemePresets: List<ThemePreset> = listOf(
    ThemePreset("dusk", "Dusk", DuskDark, DuskLight),
    ThemePreset("ember", "Ember", EmberDark, EmberLight),
    ThemePreset("monochrome", "Monochrome", MonochromeDark, MonochromeLight),
    ThemePreset("catppuccin", "Catppuccin", CatppuccinDark, CatppuccinLight),
    ThemePreset("gruvbox", "Gruvbox", GruvboxDark, GruvboxLight),
    ThemePreset("nord", "Nord", NordDark, NordLight),
    ThemePreset("rosepine", "Rosé Pine", RosepineDark, RosepineLight),
    ThemePreset("solarized", "Solarized", SolarizedDark, SolarizedLight),
    ThemePreset("dracula", "Dracula", DraculaDark, DraculaLight),
    ThemePreset("tokyonight", "Tokyo Night", TokyonightDark, TokyonightLight),
    ThemePreset("everforest", "Everforest", EverforestDark, EverforestLight),
    ThemePreset("kanagawa", "Kanagawa", KanagawaDark, KanagawaLight),
)
