import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.cmpm.tv"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.cmpm.tv"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val hivemqBroker = localProperties.getProperty("HIVEMQ_BROKER_URL") ?: "\"\""
        val hivemqUser = localProperties.getProperty("HIVEMQ_USERNAME") ?: "\"\""
        val hivemqPass = localProperties.getProperty("HIVEMQ_PASSWORD") ?: "\"\""

        buildConfigField("String", "HIVEMQ_BROKER_URL", hivemqBroker)
        buildConfigField("String", "HIVEMQ_USERNAME", hivemqUser)
        buildConfigField("String", "HIVEMQ_PASSWORD", hivemqPass)
        
        val neonApiKey = localProperties.getProperty("NEON_API_KEY")?.removeSurrounding("\"") ?: ""
        val neonHost = localProperties.getProperty("NEON_HOST")?.removeSurrounding("\"") ?: ""
        val neonUser = localProperties.getProperty("NEON_USER")?.removeSurrounding("\"") ?: ""
        val neonPass = localProperties.getProperty("NEON_PASS")?.removeSurrounding("\"") ?: ""
        
        buildConfigField("String", "NEON_API_KEY", "\"$neonApiKey\"")
        buildConfigField("String", "NEON_HOST", "\"$neonHost\"")
        buildConfigField("String", "NEON_USER", "\"$neonUser\"")
        buildConfigField("String", "NEON_PASS", "\"$neonPass\"")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // MQTT & Serialization
    implementation(libs.paho.client)
    implementation(libs.paho.service)
    implementation(libs.kotlinx.serialization.json)
    
    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}