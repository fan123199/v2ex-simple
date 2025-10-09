// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    extra["kotlinVersion"] =  "2.2.20"
}
plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("com.android.application") version "8.13.0" apply false
    id ("com.google.gms.google-services") version "4.4.4" apply false
    id ("com.google.firebase.firebase-perf") version "2.0.1" apply false
    id ("com.google.firebase.crashlytics") version "3.0.6" apply false

}