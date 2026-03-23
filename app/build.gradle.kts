import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    jacoco
    id("org.sonarqube")
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

jacoco {
    toolVersion = "0.8.12"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-analytics")
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
        "android/**/*.*"
    )

    val buildDirectory = layout.buildDirectory.get().asFile

    val javaClasses = fileTree("${buildDirectory}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(excludes)
    }
    val kotlinClasses = fileTree("${buildDirectory}/tmp/kotlin-classes/debug") {
        exclude(excludes)
    }

    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    executionData.setFrom(
        fileTree(buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}

// SonarCloud Configuration
sonarqube {
    properties {
        // Project identity - support both env vars and hardcoded defaults
        property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY") ?: "DevoopsSOEN345_ticketbooking")
        property("sonar.organization", System.getenv("SONAR_ORG") ?: "devoopssoen345")
        property("sonar.host.url", "https://sonarcloud.io")

        // Authentication - support env var for CI environments
        System.getenv("SONAR_TOKEN")?.let {
            property("sonar.login", it)
        }

        // Source and test locations
        property("sonar.sources", "src/main/java,src/main/kotlin")
        property("sonar.tests", "src/test/java,src/androidTest/java")

        // JaCoCo coverage
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

        // Java binaries - critical for SonarQube analysis
        property("sonar.java.binaries", "build/intermediates/javac/debug/compileDebugJavaWithJavac/classes,build/tmp/kotlin-classes/debug")

        // Unit test results
        property("sonar.junit.reportPaths", "build/test-results/testDebugUnitTest")

        // Exclusions
        property("sonar.exclusions", "**/*Test.java,**/*Test.kt,**/BuildConfig.java")
    }
}

