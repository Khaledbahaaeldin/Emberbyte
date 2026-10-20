plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

import java.security.MessageDigest
import java.util.zip.ZipFile

android {
    namespace = "io.github.khaledbahaaeldin.emberbyte"
    compileSdk = 37
    defaultConfig {
        applicationId = "io.github.khaledbahaaeldin.emberbyte"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = null
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions { unitTests.isIncludeAndroidResources = true }

    sourceSets.getByName("main") {
        res.directories.add("${project.layout.buildDirectory.get().asFile}/generated/res/licenses")
    }
}

val generateLicensesRaw = tasks.register<Copy>("generateLicensesRaw") {
    from(rootProject.file("NOTICE")) {
        rename { "notice.txt" }
    }
    from(rootProject.file("licenses/roboto-flex-OFL.txt")) {
        rename { "roboto_flex_ofl.txt" }
    }
    into(layout.buildDirectory.dir("generated/res/licenses/raw"))
}

tasks.named("preBuild") {
    dependsOn(generateLicensesRaw)
}

tasks.matching { it.name == "testDebugUnitTest" }.configureEach {
    val testTask = this as? Test
    inputs.dir(rootProject.file("fastlane")).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(rootProject.file("NOTICE")).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(rootProject.file("docs/release/dependency-licences.txt")).withPathSensitivity(PathSensitivity.RELATIVE)
    mustRunAfter(tasks.matching { it.name == "packageRelease" })

    val isVerifyReleaseApk = gradle.startParameter.taskNames.any { it.contains("verifyReleaseApk") }
    if (isVerifyReleaseApk) {
        testTask?.systemProperty("emberbyte.verifyReleaseApk", "true")
        testTask?.inputs?.file(layout.buildDirectory.file("outputs/apk/release/app-release-unsigned.apk"))
            ?.withPathSensitivity(PathSensitivity.RELATIVE)
            ?.optional()
    }
}

fun licenceSourcesDigest(): String {
    val root = rootProject.projectDir
    val files = mutableListOf(File(root, "docs/release/dependency-licences.txt"), File(root, "NOTICE"))
    files += File(root, "licenses").walkTopDown().filter { it.isFile }
        .sortedBy { it.relativeTo(root).invariantSeparatorsPath }.toList()
    val md = MessageDigest.getInstance("SHA-256")
    for (f in files) {
        md.update(f.relativeTo(root).invariantSeparatorsPath.toByteArray(Charsets.UTF_8))
        md.update(0)
        md.update(String(f.readBytes(), Charsets.UTF_8).replace("\r\n", "\n").toByteArray(Charsets.UTF_8))
        md.update(0)
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}

val verifyLicencesAsset = tasks.register("verifyLicencesAsset") {
    description = "Verifies that app/src/main/assets/licences.json is present and was generated from the current licence sources"
    group = "verification"
    inputs.file(rootProject.file("docs/release/dependency-licences.txt"))
    inputs.file(rootProject.file("NOTICE"))
    inputs.dir(rootProject.file("licenses"))
    inputs.file(project.file("src/main/assets/licences.json"))
    inputs.file(project.file("licences.sources.sha256"))
    doLast {
        val assetFile = project.file("src/main/assets/licences.json")
        if (!assetFile.exists() || assetFile.length() == 0L) {
            throw GradleException("Licences asset missing or empty at ${assetFile.absolutePath}. Run python tools/gen-licences.py")
        }
        val digestFile = project.file("licences.sources.sha256")
        if (!digestFile.exists()) {
            throw GradleException("licences.sources.sha256 is missing. Run python tools/gen-licences.py")
        }
        // Content based, not timestamp based: a fresh clone gives every file the checkout time.
        if (digestFile.readText().trim() != licenceSourcesDigest()) {
            throw GradleException("Licences asset ${assetFile.name} was generated from different licence sources than the current dependency-licences.txt, NOTICE or licenses/. Run python tools/gen-licences.py to regenerate it.")
        }
        logger.lifecycle("Licences asset verified: ${assetFile.name} (${assetFile.length()} bytes)")
    }
}

val verifyReleaseApk = tasks.register("verifyReleaseApk") {
    description = "Verifies the unsigned release APK matches contract requirements"
    group = "verification"
    dependsOn("assembleRelease")
    dependsOn(verifyLicencesAsset)
    dependsOn(tasks.matching { it.name == "testDebugUnitTest" })
    inputs.file(layout.buildDirectory.file("outputs/apk/release/app-release-unsigned.apk"))
    doLast {
        val apkFile = layout.buildDirectory.file("outputs/apk/release/app-release-unsigned.apk").get().asFile
        if (!apkFile.exists() || apkFile.length() == 0L) {
            throw GradleException("Release APK does not exist or is empty at ${apkFile.absolutePath}")
        }
        val forbiddenStrings = listOf("debug_fake_now", "WidgetPreviewActivity")
        ZipFile(apkFile).use { zip ->
            val dexEntries = zip.entries().asSequence().filter { it.name.endsWith(".dex") }.toList()
            if (dexEntries.isEmpty()) {
                throw GradleException("Release APK has no dex files")
            }
            for (entry in dexEntries) {
                val bytes = zip.getInputStream(entry).use { it.readBytes() }
                for (forbidden in forbiddenStrings) {
                    val target = forbidden.toByteArray(Charsets.UTF_8)
                    var found = false
                    if (bytes.size >= target.size) {
                        for (i in 0..bytes.size - target.size) {
                            var match = true
                            for (j in target.indices) {
                                if (bytes[i + j] != target[j]) {
                                    match = false
                                    break
                                }
                            }
                            if (match) {
                                found = true
                                break
                            }
                        }
                    }
                    if (found) {
                        throw GradleException("Release APK dex ${entry.name} contains forbidden string: $forbidden")
                    }
                }
            }
        }
        logger.lifecycle("Release APK verified: ${apkFile.name} (${apkFile.length()} bytes)")
    }
}

dependencies {
    implementation(project(":ui:design"))
    implementation(project(":core:engine"))
    implementation(project(":core:data"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
