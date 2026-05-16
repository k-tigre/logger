plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":logger:core"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}

android {
    namespace = "by.tigre.logger.crashlytics"
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
                artifactId = "logger-crashlytics"
                from(components["release"])
            }
        }
    }
}
