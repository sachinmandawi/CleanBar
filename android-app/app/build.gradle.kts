plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cleanbar.hider"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cleanbar.hider"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("cleanbarRelease") {
            storeFile = file("../cleanbar.jks")
            storePassword = "cleanbarpassword"
            keyAlias = "cleanbar"
            keyPassword = "cleanbarpassword"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("cleanbarRelease")
        }
        release {
            signingConfig = signingConfigs.getByName("cleanbarRelease")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Shizuku Integration
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
}
