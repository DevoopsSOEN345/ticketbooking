// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id ("org.sonarqube") version "5.1.0.4882"
}

sonarqube {
    properties {
        property("sonar.sources", "app/src/main/java")
        property("sonar.tests", "app/src/test/java")
        property("sonar.java.binaries", "app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes")
    }
}

