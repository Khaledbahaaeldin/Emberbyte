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
)

enum class UpdateSource(val displayName: String) {
    Obtainium("Obtainium"),
    OrionStore("Orion Store"),
    FDroid("F-Droid"),
    Other("Other"),
    Unknown("Unknown");

    val isKnownUpdater: Boolean
        get() = this == Obtainium || this == OrionStore || this == FDroid
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

    internal fun readInstallerPackageName(
        pm: PackageManager,
        packageName: String,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): String? {
        return try {
            if (sdkInt >= Build.VERSION_CODES.R) {
                val info = pm.getInstallSourceInfo(packageName)
                val initiating = info.initiatingPackageName
                val installing = info.installingPackageName
                if (classifyInstaller(initiating).isKnownUpdater) {
                    initiating
                } else {
                    installing ?: initiating
                }
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getInstallerPackageName(context: Context): String? {
        return try {
            readInstallerPackageName(context.packageManager, context.packageName)
        } catch (_: Exception) {
            null
        }
    }

    fun getUpdateSource(context: Context): UpdateSource {
        return classifyInstaller(getInstallerPackageName(context))
    }
}
