import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.google.services)
}

// Load local.properties once
val localProps = Properties().apply {
    file("../local.properties").takeIf { it.exists() }
        ?.inputStream()
        ?.use { load(it) }
}

// ① Load HF_API_TOKEN (prefers local.properties, then Gradle prop, then ENV)
val hfToken: String = localProps.getProperty("HF_API_TOKEN")
    ?: project.findProperty("HF_API_TOKEN") as String?
    ?: System.getenv("HF_API_TOKEN")
    ?: ""

// ② Load GROQ_API_KEY (prefers local.properties, then Gradle prop, then ENV)
val groqToken: String = localProps.getProperty("GROQ_API_KEY")
    ?: project.findProperty("GROQ_API_KEY") as String?
    ?: System.getenv("GROQ_API_KEY")
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

        // Inject tokens into BuildConfig
        buildConfigField("String", "HF_API_TOKEN", "\"$hfToken\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqToken\"")
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
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        viewBinding = true
        compose     = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("androidx.compose.material:material-icons-extended:<version>")

    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation(libs.androidx.navigation.fragment)
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation("io.coil-kt:coil-compose:2.4.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


