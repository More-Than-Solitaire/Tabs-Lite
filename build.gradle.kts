buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.3.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
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
