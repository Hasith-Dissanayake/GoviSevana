plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "lk.javainstitute.govisevana"
    compileSdk = 35

    defaultConfig {
        applicationId = "lk.javainstitute.govisevana"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.hbb20:ccp:2.5.0")


    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    implementation("com.google.firebase:firebase-auth")

    implementation ("com.google.firebase:firebase-firestore")

    implementation ("com.google.firebase:firebase-storage")


    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.squareup.picasso:picasso:2.8")


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation ("com.google.firebase:firebase-messaging")


    implementation ("com.github.PayHereDevs:payhere-android-sdk:v3.0.17")
    implementation ("androidx.appcompat:appcompat:1.6.0")
    implementation ("com.google.code.gson:gson:2.8.0")

    implementation ("androidx.sqlite:sqlite:2.1.0")


    implementation ("com.github.PayHereDevs:payhere-android-sdk:v3.0.17")
    implementation ("androidx.appcompat:appcompat:1.6.0")
    implementation ("com.google.code.gson:gson:2.8.0")


    implementation("com.google.android.gms:play-services-maps:19.0.0")





}