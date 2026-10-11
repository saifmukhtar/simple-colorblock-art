import java.util.Properties
import java.io.FileInputStream

val secretProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    secretProperties.load(FileInputStream(localPropertiesFile))
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Change the default "app" prefix to "sca" for the final APKs
base.archivesName.set("sca")

android {
    namespace = "com.simple.colorblockart"
    compileSdk = 34

    flavorDimensions += "user"
    productFlavors {
        create("him") {
            dimension = "user"
            buildConfigField("String", "TARGET_DEVICE", "\"her\"")
        }
        create("her") {
            dimension = "user"
            buildConfigField("String", "TARGET_DEVICE", "\"him\"")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("release.keystore")
            storePassword = "sca_secret_pass"
            keyAlias = "sca_release_key"
            keyPassword = "sca_secret_pass"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    defaultConfig {
        applicationId = "com.simple.colorblockart"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        buildConfigField("String", "WORKER_URL", "\"${secretProperties.getProperty("WORKER_URL", "")}\"")
        buildConfigField("String", "AUTH_TOKEN", "\"${secretProperties.getProperty("AUTH_TOKEN", "")}\"")
        // TARGET_DEVICE is now handled by the productFlavors above
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Jetpack Glance for Widgets
    implementation("androidx.glance:glance-appwidget:1.0.0")
    
    // Explicit Compose dependencies to prevent AGP mutation bugs
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // Explicit Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    
    // Firebase Cloud Messaging (Minimal)
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    
    // Coroutines for async operations (Cloudflare webhook)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // OkHttp for the webhook
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
