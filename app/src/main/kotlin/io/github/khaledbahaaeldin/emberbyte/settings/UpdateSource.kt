package io.github.khaledbahaaeldin.emberbyte.settings

import android.content.Context
import android.os.Build

val OBTAINIUM_PACKAGES = setOf(
    "dev.imranr.obtainium",
    "dev.imranr.obtainium.fdroid",
)

val ORION_STORE_PACKAGES = setOf(
    "com.orion.store",
)

val FDROID_PACKAGES = setOf(
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
    fun getInstallerPackageName(
        context: Context,
        targetPackage: String = context.packageName,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): String? {
        return try {
            val pm = context.packageManager
            if (sdkInt >= Build.VERSION_CODES.R) {
                val info = pm.getInstallSourceInfo(targetPackage)
                info.installingPackageName ?: info.initiatingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(targetPackage)
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun getUpdateSource(
        context: Context,
        targetPackage: String = context.packageName,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): UpdateSource {
        return classifyInstaller(getInstallerPackageName(context, targetPackage, sdkInt))
    }
}
