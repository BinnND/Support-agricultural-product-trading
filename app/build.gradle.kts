plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.1"
    id("com.google.firebase.crashlytics") version "3.0.2"
}

android {
    namespace = "com.example.htgdnss"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.htgdnss"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase BOM (QUAN TRỌNG - chỉ cần 1 dòng này)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // RecyclerView & ViewBinding
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")

    // Location & Images
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.play.services.maps)
    implementation(libs.play.services.analytics.impl)
    implementation(libs.camera.view)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // camera
    implementation ("androidx.camera:camera-core:1.3.3")
    implementation ("androidx.camera:camera-camera2:1.3.3")
    implementation ("androidx.camera:camera-lifecycle:1.3.3")
    implementation ("androidx.camera:camera-view:1.3.3")
    // osm
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    //gms
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    // gson
    implementation ("com.google.code.gson:gson:2.10.1")


}