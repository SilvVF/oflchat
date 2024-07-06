import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.oflchat.android.application)
    alias(libs.plugins.oflchat.android.application.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.protobuf)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("io.silv")
        }
    }
}

android {
    namespace = "io.silv.oflchat"

    defaultConfig {
        applicationId = "io.silv.oflchat"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildFeatures {
        buildConfig = true
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.util)

    implementation(libs.androidx.material.icons.extended.android)

    testImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.testManifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(libs.kotlinx.datetime)

    // Google Services
    implementation(libs.play.services.nearby)

    implementation(libs.voyager.navigator)
    implementation(libs.voyager.screenModel)
    implementation(libs.voyager.transitions)
    implementation(libs.voyager.tabNavigator)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Timber - Logging
    implementation(libs.timber)

    // SqlDelight
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.extensions.coroutines)

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.okio)

    // protobuf
    implementation(libs.protobuf.javalite)

    //camerax
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)
    implementation(libs.webrtc)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4"
    }

    generateProtoTasks {
        all().configureEach {
            builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-opt-in=coil.annotation.ExperimentalCoilApi",
        )
    }
}