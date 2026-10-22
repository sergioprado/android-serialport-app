plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.labworks.serialportapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.labworks.serialportapp"
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // SerialPortManager API built by the AOSP tree, provided at runtime by uses-library
    compileOnly(files("/opt/labs/ex/android/out/soong/.intermediates/vendor/labworks/superdroid/serialport-service/SerialPortManager/vendor.labworks.serialportmanager/android_common/combined/vendor.labworks.serialportmanager.jar"))
    implementation(libs.appcompat)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.material)
}
