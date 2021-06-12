// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        maven("https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
    }
}



allprojects {
    repositories {
        google()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
}