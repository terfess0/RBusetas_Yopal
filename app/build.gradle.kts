plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.terfess.busetasyopal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.terfess.busetasyopal"
        minSdk = 24
        targetSdk = 34
        versionCode = 24
        versionName = "1.1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // r8, ofuscacion, optimizacion y borrado de clases sin uso
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
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.maps.android:android-maps-utils:2.3.0")//maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")//maps
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0") //maps
    implementation("com.google.android.gms:play-services-location:21.3.0") //ubicacion
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    // firebase cloud messagin
    implementation("com.google.firebase:firebase-messaging:24.0.0")


    //coroutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //anuncios admob
    implementation("com.google.android.gms:play-services-ads:23.3.0")

    //splash
    implementation("androidx.core:core-splashscreen:1.0.1")
}


