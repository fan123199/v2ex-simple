// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    extra["kotlinVersion"] =  "1.8.10"
    repositories {
        google()
        mavenCentral()
//        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4")
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