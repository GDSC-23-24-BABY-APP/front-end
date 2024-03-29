// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.3.10")
    }
}

plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0-Beta4" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}
