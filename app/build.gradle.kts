plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.projectdatmonan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.projectdatmonan"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    viewBinding {
        enable=true
    }
}

dependencies {
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation ("org.mongodb:mongodb-driver-sync:4.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation ("com.google.firebase:firebase-auth:22.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
}