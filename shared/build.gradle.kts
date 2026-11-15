import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    android {
        namespace = "com.enzoftware.translatorapp"
        compileSdk {
            version = release(37) { minorApiLevel = 2 }
        }
        minSdk = 24

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        // Flag drawables referenced by UiLanguage live in androidMain/res.
        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        // Room + bundled SQLite need a real Android runtime, so the shared suite (including
        // HistoryDatabaseContract) also runs on a device. Joining the "test" source-set tree makes
        // androidDeviceTest inherit commonTest through the default hierarchy (no manual dependsOn,
        // which would switch that hierarchy off and break the iOS source sets).
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // No iosX64: Room 3 and the bundled SQLite driver don't ship Intel-simulator binaries.
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.ktor)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlin.date.time)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.ext.junit)
        }

        androidMain.dependencies {
            implementation(libs.ktor.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.darwin)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Room's KSP processor runs once per target that compiles the database.
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { add(it, libs.room.compiler) }
}
