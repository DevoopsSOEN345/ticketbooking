// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id ("org.sonarqube") version "5.1.0.4882"
}

sonar {
    // This prevents the root project from crashing because it's not an Android app
    isSkipProject = true
}


// sonar {
//     properties {
//         // Force manual source config to bypass Android plugin detection
//         property("sonar.sources", "app/src/main/java")
//         property("sonar.tests", "app/src/test/java")
//         property("sonar.java.binaries", "app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes")
//         property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
//         property("sonar.android.lint.report", "app/build/reports/lint-results-debug.xml")
//     }
// }

