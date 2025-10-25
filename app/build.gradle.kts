@file:Suppress("LocalVariableName", "PropertyName")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
    jvmToolchain(17)
}

android {

    compileSdk = 36
    defaultConfig {
        applicationId = "im.fdx.v2ex"
        minSdk = 24
        targetSdk = 36
        versionCode = 75
        versionName = "2.9.10"
    }
    lint {
        checkReleaseBuilds = false
        abortOnError =   false
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
    signingConfigs {

        create("googlePlay") {
            // 从根目录加载 keystore.properties
            val propertiesFile = rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                val properties = Properties().apply {
                    load(propertiesFile.inputStream())
                }
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
                storeFile = file(properties.getProperty("storeFile")) // 'file()' 会自动解析相对路径
                storePassword = properties.getProperty("storePassword")
            } else {
                // 当文件不存在时，可以打印警告或让构建失败，避免后续出现 Null-Pointer 异常
                println("Warning: keystore.properties not found. Release builds may fail.")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix =  ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("googlePlay" )
        }
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "im.fdx.v2ex"
}

//configurations.all {
//    resolutionStrategy {
//        force( "org.jetbrains.kotlin:kotlin-parcelize-runtime:2.2.20")
//    }
//}

android.applicationVariants.all { variant ->
    variant.outputs
        .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
        .forEach { output ->
            if(variant.buildType.name == "debug") {
                output.outputFileName = "v2ex-${variant.buildType.name}-${variant.versionName}.apk"
            }
        }
    true
}

dependencies {


    //test related, use test when you really need it, or you forget always.
    testImplementation("junit:junit:4.13.2")
    //test end

    //kotlin start
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("androidx.core:core-ktx:1.17.0")
    //kotlin end

    //google start
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.9.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.work:work-runtime-ktx:2.10.5")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    val roomVersion = "2.8.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp ("androidx.room:room-compiler:$roomVersion")
    //google end

    implementation("com.google.code.gson:gson:2.13.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.2.1"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    debugImplementation("com.github.chuckerteam.chucker:library:4.2.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.2.0")
    implementation("com.elvishew:xlog:1.11.1")
    implementation("org.jsoup:jsoup:1.21.2")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    ksp ("com.github.bumptech.glide:compiler:5.0.5")
    implementation("com.github.bumptech.glide:okhttp3-integration:5.0.5")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    implementation("me.drakeet.multitype:multitype:3.5.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
//    implementation("com.github.esafirm:android-image-picker:3.1.0")
    implementation("com.github.esafirm.android-image-picker:imagepicker:2.4.5"){
        exclude("org.jetbrains.kotlin", "kotlin-android-extensions-runtime")
    }

    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation ("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-perf")

}