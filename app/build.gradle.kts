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
        versionCode = 10
        versionName = "1.0.4"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding{
        enable=true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.maps.android:android-maps-utils:2.3.0")//maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")//maps
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0") //maps
    implementation("com.google.android.gms:play-services-location:21.0.1") //ubicacion
    implementation("androidx.cardview:cardview:1.0.0") //maps

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    //api google places
    implementation("com.google.android.libraries.places:places:3.3.0")

    //coroutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    //anuncios admob
    implementation("com.google.android.gms:play-services-ads:22.6.0")


}


