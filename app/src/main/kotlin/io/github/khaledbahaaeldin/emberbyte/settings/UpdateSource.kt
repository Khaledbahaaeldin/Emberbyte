package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

private val OBTAINIUM_PACKAGES = setOf(
    "dev.imranr.obtainium",
    "dev.imranr.obtainium.fdroid",
)

private val ORION_STORE_PACKAGES = setOf(
    "com.orion.store",
)

private val FDROID_PACKAGES = setOf(
    "org.fdroid.fdroid",
    "org.fdroid.basic",
    "org.fdroid.fdroid.privileged",
    "org.fdroid.fdroid.classic",
)

enum class UpdateSource(val displayName: String) {
    Obtainium("Obtainium"),
    OrionStore("Orion Store"),
    FDroid("F-Droid"),
    Other("Other"),
    Unknown("Unknown");

    val isKnownUpdater: Boolean
        get() = this == Obtainium || this == OrionStore || this == FDroid

    val subtitle: String
        get() = when (this) {
            Obtainium -> "Managed by Obtainium"
            OrionStore -> "Managed by Orion Store"
            FDroid -> "Managed by F-Droid"
            Other, Unknown -> "GitHub Releases"
        }
}

fun classifyInstaller(installerPackageName: String?): UpdateSource {
    val trimmed = installerPackageName?.trim()
    if (trimmed.isNullOrEmpty()) {
        return UpdateSource.Unknown
    }
    return when (trimmed) {
        in OBTAINIUM_PACKAGES -> UpdateSource.Obtainium
        in ORION_STORE_PACKAGES -> UpdateSource.OrionStore
        in FDROID_PACKAGES -> UpdateSource.FDroid
        else -> UpdateSource.Other
    }
}

object InstallSourceReader {

    internal fun resolveInstaller(
        pm: PackageManager,
        targetPackage: String,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): String? {
        return try {
            if (sdkInt >= Build.VERSION_CODES.R) {
                val info = pm.getInstallSourceInfo(targetPackage)
                val initiating = info.initiatingPackageName
                val installing = info.installingPackageName
                when {
                    classifyInstaller(initiating).isKnownUpdater -> initiating
                    classifyInstaller(installing).isKnownUpdater -> installing
                    else -> installing ?: initiating
                }
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(targetPackage)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getInstallerPackageName(context: Context): String? {
        return try {
            resolveInstaller(context.packageManager, context.packageName)
        } catch (_: Exception) {
            null
        }
    }

    fun getUpdateSource(context: Context): UpdateSource {
        return classifyInstaller(getInstallerPackageName(context))
    }
}
