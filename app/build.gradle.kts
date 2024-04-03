plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.geminipro"
    compileSdk = 34

    buildFeatures.buildConfig = true
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.geminipro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("io.coil-kt:coil:1.4.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.1.1")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-rxjava2:2.6.1")


    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")

    //rx
    implementation ("com.uber.autodispose:autodispose:1.4.0")
    implementation ("com.uber.autodispose:autodispose-android-archcomponents:1.4.0")

    //flexbox
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    //gif
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.28")

    //swiperefreshlayout
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //viewpager2
    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    //photoview
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    //Gson
    implementation ("com.google.code.gson:gson:2.10.1")
}