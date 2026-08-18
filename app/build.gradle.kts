// SPDX-License-Identifier: GPL-3.0-or-later
plugins {
    alias(libs.plugins.android.application)
    // AGP 9's built-in Kotlin support compiles Kotlin sources without a separate
    // org.jetbrains.kotlin.android plugin (see AGP 9.0 release notes).
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.androidpods.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.androidpods.app"
        minSdk = 36
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-alpha"
    }

    signingConfigs {
        val keystorePath = System.getenv("KEYSTORE_PATH") ?: findProperty("ANDROIDPODS_KEYSTORE_PATH") as? String
        val keystorePassword = System.getenv("KEYSTORE_PASSWORD") ?: findProperty("ANDROIDPODS_KEYSTORE_PASSWORD") as? String
        val keyAlias = System.getenv("KEY_ALIAS") ?: findProperty("ANDROIDPODS_KEY_ALIAS") as? String
        val keyPassword = System.getenv("KEY_PASSWORD") ?: findProperty("ANDROIDPODS_KEY_PASSWORD") as? String

        if (keystorePath != null && file(keystorePath).exists()) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
        create("benchmark") {
            initWith(getByName("release"))
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        create("baselineProfile") {
            // Baseline Profile plugin 1.4.1 still uses removed pre-AGP-9 APIs.
            initWith(getByName("release"))
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hiddenapibypass)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.profileinstaller)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hiddenapibypass)
}
