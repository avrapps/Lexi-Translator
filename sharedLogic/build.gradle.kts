import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }

    jvm()

    js {
        outputModuleName = "sharedLogic"
        browser()
        binaries.library()
        generateTypeScriptDefinitions()
        compilerOptions {
            target = "es2015"
            optIn.add("kotlin.js.ExperimentalJsExport")
        }
    }

    androidLibrary {
        namespace = "com.falconlabs.aitranslator.sharedLogic"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.sqlcipher.android)
            implementation(libs.onnxruntime.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvmDriver)
            implementation(libs.onnxruntime.jvm)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.sqldelight.jsDriver)
        }
    }
}

sqldelight {
    databases {
        create("LexiDatabase") {
            packageName.set("com.falconlabs.aitranslator.db")
        }
    }
}

// SQLDelight generates code that doesn't conform to ktlint rules.
// Disable all source set checks that include generated code from build/.
afterEvaluate {
    tasks.matching { task ->
        task.name.startsWith("ktlint") &&
            task.name.contains("SourceSet") &&
            (task.name.contains("Check") || task.name.contains("Format"))
    }.configureEach {
        onlyIf {
            val sources = inputs.files.files
            val allFromSrc = sources.isEmpty() ||
                sources.all { file ->
                    !file.path.contains("${File.separator}build${File.separator}")
                }
            allFromSrc
        }
    }
}
