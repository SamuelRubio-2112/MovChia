plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // Firebase: Plugin de Google Services para leer google-services.json
    alias(libs.plugins.google.services)
}

// Si falta google-services.json (clon nuevo), se copia el placeholder para que
// el proyecto compile. Reemplázalo por el archivo real de la consola de Firebase.
val googleServicesFile = file("google-services.json")
if (!googleServicesFile.exists()) {
    file("google-services.example.json").copyTo(googleServicesFile)
    logger.warn("MovChia: se creó app/google-services.json desde el placeholder. Firebase NO funcionará hasta usar el archivo real.")
}

android {
    namespace = "com.movchia.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.movchia.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose (BOM controla las versiones)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation Compose (rutas tipadas con kotlinx-serialization)
    implementation(libs.androidx.navigation.compose)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    // Firebase (BOM y dependencias)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    // Media3: reproductor de video (MP4/HLS)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // Coil: imágenes desde URL / Figma / Cloud Storage
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
