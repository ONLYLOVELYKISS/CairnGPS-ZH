import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.matthieu.cairngps"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "app.matthieu.cairngps"
        minSdk = 26
        targetSdk = 37
        versionCode = (project.property("VERSION_CODE") as String).toInt()
        versionName = project.property("VERSION_NAME") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Always publish one installable APK containing every supported ABI. This prevents a split APK
    // (or a base APK without its native config split) from being distributed as the standalone file.
    splits {
        abi {
            isEnable = false
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    // AGP strips native libraries by default even when they're prebuilt binaries pulled in from a
    // dependency's AAR (e.g. datastore's libdatastore_shared_counter.so) rather than compiled by
    // this project. The stripped output depends on the exact NDK strip tool available in the build
    // environment, so it isn't reproducible across machines — F-Droid's rebuild produces different
    // bytes for the same .so than ours. Keeping them unstripped removes that variable entirely.
    packaging {
        jniLibs {
            // Keep native libraries in the APK for broad compatibility with standalone sideloading.
            useLegacyPackaging = true
            keepDebugSymbols.add("**/*.so")
        }
    }

    signingConfigs {
        val keystorePath = providers.environmentVariable("KEYSTORE_FILE").orNull
        if (!keystorePath.isNullOrBlank()) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
    buildFeatures {
        compose = true
        // BuildConfig is used for app version metadata and runtime diagnostics.
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

tasks.register("verifyUniversalDebugApk") {
    dependsOn("assembleDebug")

    doLast {
        val outputDir = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        val metadataFile = outputDir.resolve("output-metadata.json")
        val apkFile = outputDir.resolve("app-debug.apk")

        check(metadataFile.isFile) { "Missing APK output metadata: $metadataFile" }
        check(apkFile.isFile) { "Missing universal APK: $apkFile" }

        val metadata = metadataFile.readText()
        check(metadata.contains("\"applicationId\": \"app.matthieu.cairngps\"")) {
            "APK metadata has no expected applicationId"
        }
        check(metadata.contains("\"type\": \"SINGLE\"")) {
            "APK output is not a single universal APK"
        }
        check(metadata.contains("\"filters\": []")) {
            "APK output contains ABI or configuration filters"
        }

        ZipFile(apkFile).use { apk ->
            val requiredEntries = listOf("AndroidManifest.xml", "classes.dex", "resources.arsc")
            requiredEntries.forEach { entry ->
                check(apk.getEntry(entry) != null) { "APK is missing required entry: $entry" }
            }

            val requiredAbis = setOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            val includedAbis = apk.entries().asSequence()
                .map { it.name }
                .mapNotNull { name ->
                    if (!name.startsWith("lib/") || !name.endsWith(".so")) return@mapNotNull null
                    name.substringAfter("lib/").substringBefore('/')
                }
                .toSet()
            check(requiredAbis.all(includedAbis::contains)) {
                "Universal APK is missing an ABI: expected $requiredAbis, found $includedAbis"
            }
        }

        logger.lifecycle("Verified universal APK: ${apkFile.absolutePath}")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // ProcessLifecycleOwner: gates GamificationManager's live GPS subscription to app-foreground.
    implementation(libs.androidx.lifecycle.process)

    // Navigation & persistence
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    // Room (local database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Backup export/import (Settings)
    implementation(libs.kotlinx.serialization.json)

    // Per-app language switching (AppCompatDelegate.setApplicationLocales)
    implementation(libs.androidx.appcompat)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
