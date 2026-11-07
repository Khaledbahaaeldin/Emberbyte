package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Context
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape

/** Where the app was installed from, as far as Android tells us. */
enum class InstallerKind { Obtainium, OrionStore, FDroid, Other, Unknown }

/** Opened when the user wants to look for a newer version without an updater app. */
internal const val RELEASES_URL = "https://github.com/Khaledbahaaeldin/Emberbyte/releases/latest"

private val OBTAINIUM_PACKAGES = setOf("dev.imranr.obtainium", "dev.imranr.obtainium.fdroid")
private const val ORION_PACKAGE = "com.orion.store"
private val FDROID_PACKAGES = setOf("org.fdroid.fdroid", "org.fdroid.basic")

internal fun classifyInstaller(packageName: String?): InstallerKind = when {
    packageName.isNullOrBlank() -> InstallerKind.Unknown
    packageName in OBTAINIUM_PACKAGES -> InstallerKind.Obtainium
    packageName == ORION_PACKAGE -> InstallerKind.OrionStore
    packageName in FDROID_PACKAGES -> InstallerKind.FDroid
    else -> InstallerKind.Other
}

/**
 * Reads the installer package. Any failure counts as "unknown": this only chooses a wording, so it must never crash.
 * Uses the install-source API on Android 11+ and the older call below that (minSdk is 29).
 */
class InstallSourceReader(private val lookup: () -> String?) {
    fun installerPackage(): String? = runCatching(lookup).getOrNull()

    fun kind(): InstallerKind = classifyInstaller(installerPackage())

    companion object {
        /** [sdkInt] is injectable so the Android 10 branch can be tested without a second Robolectric SDK. */
        fun forContext(context: Context, sdkInt: Int = Build.VERSION.SDK_INT): InstallSourceReader = InstallSourceReader {
            val pm = context.packageManager
            if (sdkInt >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(context.packageName)
            }
        }
    }
}

data class UpdatesUiState(
    val kind: InstallerKind,
    val versionName: String,
    /** The installer's package, used to open it. Null when unknown. */
    val installerPackage: String? = null,
)

private fun updatesSubtitle(state: UpdatesUiState): String = when (state.kind) {
    InstallerKind.Obtainium -> "Obtainium keeps Emberbyte up to date"
    InstallerKind.OrionStore -> "Orion Store keeps Emberbyte up to date"
    InstallerKind.FDroid -> "F-Droid keeps Emberbyte up to date"
    InstallerKind.Other, InstallerKind.Unknown ->
        "You have ${state.versionName}. Tap to see the latest release on GitHub. " +
            "Emberbyte never goes online by itself, so it cannot tell you when one appears."
}

/**
 * Points the user to whoever updates the app. It cannot say whether a newer version exists, because the app has no
 * network permission by design.
 */
@Composable
fun UpdatesRow(
    state: UpdatesUiState,
    onOpenUpdater: () -> Unit,
    onOpenReleases: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val opensUpdater = state.kind == InstallerKind.Obtainium ||
        state.kind == InstallerKind.OrionStore ||
        state.kind == InstallerKind.FDroid
    SettingsRow(
        title = "Updates",
        subtitle = updatesSubtitle(state),
        iconVector = Icons.Rounded.SystemUpdate,
        iconText = null,
        iconShape = AppShape.Squircle,
        showChevron = true,
        onClick = if (opensUpdater) onOpenUpdater else onOpenReleases,
        modifier = modifier,
    )
}
