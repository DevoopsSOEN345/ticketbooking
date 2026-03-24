// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("org.sonarqube") version "7.2.3.7755"
}

sonar {
    properties {
        property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY") ?: "DevoopsSOEN345_ticketbooking")
        property("sonar.organization", System.getenv("SONAR_ORG") ?: "devoopssoen345")
        property("sonar.host.url", "https://sonarcloud.io")

        // Avoid automatic compile task dependency wiring for Android projects.
        property("sonar.gradle.skipCompile", "true")

        System.getenv("SONAR_TOKEN")?.let {
            property("sonar.login", it)
        }

        property("sonar.sources", "app/src/main/java")
        property("sonar.tests", "app/src/test/java,app/src/androidTest/java")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "**/MainActivity.java")
        property("sonar.java.binaries", "app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes")
        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")
        property("sonar.exclusions", "**/*Test.java,**/*Test.kt,**/BuildConfig.java")
    }
}

project(":app") {
    sonar {
        setSkipProject(true)
    }
}
