import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
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
            implementation(project(":logger:core"))
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.adapters)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.jvm)
            }
        }
    }
}

android {
    namespace = "by.tigre.logger.internal_store"
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

sqldelight {
    databases {
        create("DatabaseLog") {
            packageName.set("by.tigre.logger.db")
            generateAsync.set(true)
        }
    }
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication>().configureEach {
            artifactId = when (name) {
                "kotlinMultiplatform" -> "logger-internal-store"
                "desktop" -> "logger-internal-store-desktop"
                "androidRelease" -> "logger-internal-store-android"
                else -> artifactId
            }
        }
    }
}
