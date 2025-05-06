import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.google.services)
}

//
// ① Load HF_API_TOKEN from: local.properties (preferred, gitignored)
//    or from a Gradle property, or from the ENV.
//    Falls back to empty string if none is defined.
//
val hfToken: String = Properties().apply {
    file("../local.properties").takeIf { it.exists() }  // adjust path if needed
        ?.inputStream()
        ?.use { load(it) }
}.getProperty("HF_API_TOKEN")
    ?: project.findProperty("HF_API_TOKEN") as String?
    ?: System.getenv("HF_API_TOKEN")
    ?: ""
android {
    namespace   = "com.example.moodify"
    compileSdk  = 35

    defaultConfig {
        applicationId             = "com.example.moodify"
        minSdk                    = 24
        targetSdk                 = 35
        versionCode               = 1
        versionName               = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //
        // ② Inject your token into BuildConfig.HF_API_TOKEN
        //
        buildConfigField(
            "String",
            "HF_API_TOKEN",
            "\"$hfToken\""
        )
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true    // if you still use XML views
        compose     = true    // for your Jetpack Compose UI
        buildConfig = true    // ← enable custom BuildConfig fields!

    }
    composeOptions {
        // Make sure this matches the version your Compose BOM pulls in
        kotlinCompilerExtensionVersion = "1.5.6"
    }
}

dependencies {
    // Core AndroidX + Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
// 3️⃣ Material3 / Foundation / UI
    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.foundation:foundation")    // Material3 components
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Retrofit + Kotlinx-Serialization + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation(libs.androidx.navigation.fragment)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
