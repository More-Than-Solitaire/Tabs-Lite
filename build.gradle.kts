buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.5")
    }
}

plugins {
    id("com.diffplug.spotless") version "6.23.3"
}

allprojects {
    repositories {
        google()
    }
}

spotless {
    kotlin {
        target("**/*.kt")
    }
}
