pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }


    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                // maps alias(libs.plugins.android.application)
                "com.android.application" ->
                    useModule("com.android.tools.build:gradle:${requested.version}")
                // maps alias(libs.plugins.kotlin.android)
                "org.jetbrains.kotlin.android" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                // maps alias(libs.plugins.google.services)
                "com.google.gms.google-services" ->
                    useModule("com.google.gms:google-services:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Moodify"
include(":app")
 