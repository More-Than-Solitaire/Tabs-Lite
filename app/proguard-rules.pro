# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/google/home/tiem/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# SourceFile,LineNumberTable preserve the line number information for
# debugging stack traces. Signature helps with types for UgApi server handshake
-keepattributes SourceFile,LineNumberTable,Signature

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class androidx.navigation.fragment.NavHostFragment { *; }

-keep class com.gbros.tabslite.data.servertypes.** { *; }
-keepattributes Exceptions, Signature, InnerClasses, SourceFile, LineNumberTable

-keep class com.chrynan.chords.** { *; }
-keepattributes Exceptions, Signature, InnerClasses, SourceFile, LineNumberTable

# For UgApi.kt, to allow handshake response auto-typing, thanks https://stackoverflow.com/a/76224937/3437608
# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
