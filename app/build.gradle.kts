plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ✅ Google services plugin for Firebase integration
    id("com.google.gms.google-services")

    // ✅ Kotlin Kapt for annotation processing
    id("kotlin-kapt")
}

android {
    namespace = "com.example.newsreportingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.newsreportingapp"
        minSdk = 26
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true // ✅ Enables View Binding
    }
}

dependencies {
    // ✅ AndroidX & Material Components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.9.0")

    // ✅ Firebase (Using BOM for version management)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Auth with Kotlin extensions
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")

    // ✅ Google Play Services (For Google Sign-In)
    implementation("com.google.android.gms:play-services-auth:19.2.0")

    // ✅ Image Loading Libraries
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.picasso:picasso:2.8") // Remove if not needed

    // ✅ Networking Libraries
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // ✅ Coroutine Support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    // ✅ Realm Database
    implementation("io.realm:realm-android-library:10.10.1")

    // ✅ UI Enhancements
    implementation(libs.androidx.swiperefreshlayout)

    // ✅ Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
