// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    extra["kotlinVersion"] =  "1.9.10"
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath ("com.google.firebase:perf-plugin:1.4.2")
    }
}
plugins {
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}