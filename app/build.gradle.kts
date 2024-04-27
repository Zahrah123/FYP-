plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

        dependencies {
            implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
            implementation("com.google.firebase:firebase-analytics:20.0.0")
            implementation("com.google.firebase:firebase-database:20.0.0")
            implementation("com.google.ar.sceneform.ux:sceneform-ux:1.17.1")
            implementation("com.google.ar:core:1.41.0")
            implementation("com.google.android.material:material:1.11.0")
            implementation("androidx.constraintlayout:constraintlayout:2.1.4")
            implementation("androidx.core:core:1.0.0")
            implementation ("com.microsoft.azure.spatialanchors:spatialanchors_jni:[2.10.2]")
            implementation ("com.microsoft.azure.spatialanchors:spatialanchors_java:[2.10.2]")

            testImplementation("junit:junit:4.13.2")

            androidTestImplementation("androidx.core:core:1.9.0")
            androidTestImplementation("androidx.test.ext:junit:1.1.5")
            androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        }

