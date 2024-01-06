plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("maven-publish")
}

kotlin {
    jvmToolchain(11)
}


android {
    namespace = "sound.recorder.widget"
    compileSdk = 34

    defaultConfig {
        minSdk = 19
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }

        javaCompileOptions {
            annotationProcessorOptions {
                argument("includeCompileClasspath", "false")
            }
        }
        kapt {
            includeCompileClasspath = false
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            multiDexEnabled = true
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true

    }


}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    implementation ("com.google.android.gms:play-services-ads:22.6.0")
    implementation ("com.karumi:dexter:6.2.3")
    implementation ("com.airbnb.android:lottie:6.0.0")

    implementation ("androidx.room:room-runtime:2.5.2")
    //noinspection KaptUsageInsteadOfKsp
    kapt ("androidx.room:room-compiler:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
    implementation ("androidx.room:room-ktx:2.5.2")
    annotationProcessor ("androidx.room:room-compiler:2.5.2")

    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation ("com.google.firebase:firebase-bom:32.7.0")
    implementation ("org.jetbrains.kotlin:kotlin-bom:1.9.22")

    // Add the dependencies for the Firebase Cloud Messaging and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("com.google.firebase:firebase-config:21.6.0")
    //implementation 'androidx.work:work-runtime-ktx:2.8.1'
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.google.android.ump:user-messaging-platform:2.1.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
}


publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "zaif.record.sdk"
            artifactId = "zaif"
            version = "0.0.1"

            afterEvaluate {
                from(components["release"])
            }
        }

        repositories {
            maven {
                name = "RecordingSDK"
                url = uri("${project.buildDir}/repo")
            }
        }

    }
}
