plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.navigationSafeargs) apply false
    alias(libs.plugins.daggerHilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinParcelize) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.spotless)

    // Add the dependency for the Google services Gradle plugin
    alias(libs.plugins.google.services) apply false
}

spotless {
    kotlin {
        target("**/*.kt")
    }
}
