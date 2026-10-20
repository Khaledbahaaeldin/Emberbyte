package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

data class EmberbyteThemeSpec(
    val presetId: String = "dusk",
    val darkTheme: Boolean = true,
    val amoled: Boolean = false,
    val wallpaperColors: Boolean = false,
) {
    companion object {
        val Default = EmberbyteThemeSpec(
            presetId = "dusk",
            darkTheme = true,
            amoled = false,
            wallpaperColors = false,
        )
    }
}
