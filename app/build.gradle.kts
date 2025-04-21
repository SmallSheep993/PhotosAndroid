plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.photosandroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.photosandroid"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ 保留你需要的 activity 版本
    implementation("androidx.activity:activity:1.8.2")
}

// ✅ 强制 resolution（确保 1.10.1 不再被拉进来）
configurations.all {
    resolutionStrategy {
        force("androidx.activity:activity:1.8.2")
    }
}
