// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    var kotlin_version: String by extra
    kotlin_version = "1.7.20"
    repositories {
        google()
        mavenCentral()
//        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.1")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
        classpath ("com.google.firebase:perf-plugin:1.4.2")
    }
}



allprojects {
    repositories {
        google()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
}