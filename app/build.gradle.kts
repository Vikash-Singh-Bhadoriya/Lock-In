@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.hilt)
//    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.vikashsinghapp.lockin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vikashsinghapp.lockin"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        debug {
            // Disables code shrinking for the debug build type.
            // compile-time optimizations increase the build time of your project and might introduce bugs
            isMinifyEnabled = false

            applicationIdSuffix = ".debug" // Used to differentiate between multiple builds on same device

            versionNameSuffix = "-DEBUG"
            isDebuggable = true
        }
        release {
            // Enables code shrinking, obfuscation, and optimization for only
            // your project's release build type.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the Android Gradle plugin.
            isShrinkResources = true

            // ProGuard rules files helps to customize default R8’s behavior
            // help R8 better understand your app’s structure
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                // List additional ProGuard rules for the given build type here. By default,
                // Android Studio creates and includes an empty rules file for you (located
                // at the root directory of each module).
                "proguard-rules.pro"
            )
            isDebuggable = false

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Timber -> logging
    implementation(libs.timber)

    // Coroutines -> Concurrency
    implementation(libs.kotlinx.coroutines.android)

    // Reorder plan's task
    implementation(libs.reorderable)
    // Reorder plan's task
    implementation(libs.coil.kt.compose)

    // Lottie Animation
    implementation(libs.lottie.compose)

    // Room -> Local DB
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx) // to return Flow

    // DataStore -> Manage preferences (small datasets)
    // preferences Datastore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Splash Screen
    implementation(libs.splash.screen)

    // Dagger-Hilt -> DI
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // GETTING BUILD WARNINGS => Kapt currently doesn't support language version 2.0+. Falling back to 1.9.
    // USING KSP EVERYWHERE INSTEAD OF KAPT (FINALLY IN HILT WITH KSP)
    kspAndroidTest(libs.hilt.compiler)

}