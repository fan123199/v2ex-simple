// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    extra["kotlinVersion"] =  "2.3.0"
}
plugins {
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.0" apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("com.android.application") version "8.13.2" apply false
    id ("com.google.gms.google-services") version "4.4.4" apply false
    id ("com.google.firebase.firebase-perf") version "2.0.2" apply false
    id ("com.google.firebase.crashlytics") version "3.0.6" apply false
    id("androidx.room") version "2.8.4" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
}