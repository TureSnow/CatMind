plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android-extensions")
}
apply("maven-publish.gradle")
android {
    namespace = "com.ftang.catmind"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
    dataBinding {
        isEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("androidx.lifecycle:lifecycle-process:2.2.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")
}
// 配置aar文件的输出路径
artifacts {
    archives( file("build/outputs/aar/MyLibrary.aar"))
}