plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":logger:core"))
}

android {
    namespace = "by.tigre.logger.logcat"
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                artifactId = "logger-logcat"
                from(components["release"])
            }
        }
    }
}
