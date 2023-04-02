@file:Suppress("LocalVariableName", "PropertyName")

import java.util.*

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.firebase.firebase-perf")
}
apply {
    plugin("kotlin-android")
}

android {

    compileSdk = 33
    defaultConfig {
        applicationId = "im.fdx.v2ex"
        minSdk = 22
        targetSdk = 33
        versionCode = 61
        versionName = "2.9.0"
    }
    lint {
        checkReleaseBuilds = false
        abortOnError =   false
    }
    signingConfigs {

        create("googlePlay" ){
            val properties = Properties().apply {
                load(File("keystore.properties").reader())
            }
            keyAlias = properties.getProperty("keyAlias") as String
            keyPassword =  properties.getProperty("keyPassword")  as String
            storeFile =  file(properties.getProperty("storeFile") as String)
            storePassword = properties.getProperty("storePassword")  as String
        }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}


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

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    //test related, use test when you really need it, or you forget always.
    testImplementation("junit:junit:4.13.2")
    //test end

    //kotlin start
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.anko:anko-commons:0.10.8")
    implementation("androidx.core:core-ktx:1.9.0")
    //kotlin end

    //google start
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
//    implementation("androidx.legacy:legacy-support-v4:1.0.0")
//    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    val preference_version = "1.2.0"
    implementation("androidx.preference:preference-ktx:$preference_version")

    val roomVersion = "2.5.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt ("androidx.room:room-compiler:$roomVersion")
    //google end

    implementation("com.google.code.gson:gson:2.9.1")
    implementation("de.hdodenhof:circleimageview:2.2.0")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    debugImplementation("com.github.ChuckerTeam.Chucker:library:3.5.2")
    releaseImplementation("com.github.ChuckerTeam.Chucker:library-no-op:3.5.2")
    implementation("com.elvishew:xlog:1.6.1")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    kapt ("com.github.bumptech.glide:compiler:4.14.2")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.14.2")
    implementation("io.reactivex.rxjava2:rxjava:2.2.14")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("me.drakeet.multitype:multitype:3.5.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.esafirm.android-image-picker:imagepicker:2.4.5")

    implementation(platform("com.google.firebase:firebase-bom:31.2.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation ("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation ("com.google.firebase:firebase-perf-ktx")
}
repositories {
    mavenCentral()
}