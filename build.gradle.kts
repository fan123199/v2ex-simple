// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  extra["kotlin_version"] = "1.2.71"
  extra["support_version"] = "28.0.0"
  extra["anko_version"] = "0.10.5"

  repositories {
    google()
    jcenter()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.fabric.io/public") }
  }
  dependencies {
    classpath("com.google.gms:google-services:3.2.0")
    classpath("com.android.tools.build:gradle:3.3.0-alpha13")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra["kotlin_version"]}")
    classpath("io.fabric.tools:gradle:1.25.4")
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    maven { url = uri("https://jitpack.io") }

  }
}