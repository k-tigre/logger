import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
            implementation(libs.coroutines.core)
        }
    }
}

android {
    namespace = "by.tigre.logger.core"
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication>().configureEach {
            artifactId = when (name) {
                "kotlinMultiplatform" -> "logger-core"
                "desktop" -> "logger-core-desktop"
                "androidRelease" -> "logger-core-android"
                else -> artifactId
            }
        }
    }
}
