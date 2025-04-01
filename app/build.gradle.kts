plugins {
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("D:\\Code\\Android Development\\gbrosLLC-keystore.jks" )
        }
    }

    compileSdk = 35

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"  // dependant on Kotlin version; see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    }

    defaultConfig {
        applicationId = "com.gbros.tabslite"
        minSdk = 24
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 3660
        versionName = "3.6.6"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            ndk.debugSymbolLevel = "FULL"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    dependenciesInfo {
        includeInApk = false // don"t include Google signed dependency tree in APK to allow the app to be compatible with FDroid
        includeInBundle = true
    }
    namespace = "com.gbros.tabslite"
}

repositories {
    google()
    mavenCentral()
    maven(url = "https://repo.repsy.io/mvn/chrynan/public")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("androidx.activity:activity-compose:1.10.1")  // Compose Integration with activities
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.1")  // Material Design 3
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.8")
    // Compose Integration with LiveData
    implementation("androidx.compose.ui:ui-tooling-preview")  // Android Studio Preview Support
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")  // Compose Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.SmartToolFactory:Compose-Extended-Gestures:3.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-android-compiler:2.55")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    implementation("com.chrynan.chords:chords-compose:2.4.1")

    // Debug dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")  // Android Studio Preview support
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")  // UI tests

    // Testing dependencies
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")  // UI tests
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("androidx.work:work-testing:2.10.0")
    androidTestImplementation("com.google.truth:truth:1.4.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.16")
}
