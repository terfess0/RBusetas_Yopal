buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
    repositories {//anuncios
        google()
        mavenCentral()
    }
}


// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    //places
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    //kpt - ksp
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
}
