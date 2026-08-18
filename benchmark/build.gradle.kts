// SPDX-License-Identifier: GPL-3.0-or-later
plugins {
    id("com.android.test")
}

android {
    namespace = "dev.androidpods.benchmark"
    compileSdk = 37

    defaultConfig {
        minSdk = 36
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    buildTypes {
        create("benchmark") {
            initWith(getByName("debug"))
        }
        create("baselineProfile") {
            initWith(getByName("debug"))
        }
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.test.uiautomator)
}
