import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("jacoco")
}

android {
    namespace = "com.example.devoops"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.devoops"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true 
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    androidTestImplementation(libs.espresso.core)

    // Testing dependencies for 100% code coverage
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.13")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation(libs.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.0")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val excludes = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Activity.*"
    )

    val buildDirectory = layout.buildDirectory.get().asFile

    val javaClasses = fileTree("${buildDirectory}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(excludes)
    }

    classDirectories.setFrom(files(javaClasses))
    sourceDirectories.setFrom(files("src/main/java"))

    // Use the execution data from AGP's new path
    executionData.setFrom(
        fileTree(buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}


