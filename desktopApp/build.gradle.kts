import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.sharedUI)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    // Koin for Desktop
    implementation(libs.koin.core)
}

compose.desktop {
    application {
        mainClass = "com.falconlabs.aitranslator.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm,
            )
            packageName = "Lexi Translator"
            packageVersion = "1.0.0"
            description = "Offline AI Translation Platform"
            vendor = "ANRMS PRIVATE LIMITED"
            copyright = "Copyright © 2024-2026 ANRMS PRIVATE LIMITED. AGPL-3.0 License."

            linux {
                packageName = "lexi-translator"
                debMaintainer = "dev@falconlabs.com"
                appCategory = "Education"
                iconFile.set(project.file("src/main/resources/icons/app-icon.png"))
            }

            windows {
                packageName = "Lexi Translator"
                dirChooser = true
                menuGroup = "Lexi Translator"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                iconFile.set(project.file("src/main/resources/icons/app-icon.ico"))
            }

            macOS {
                packageName = "Lexi Translator"
                bundleID = "com.falconlabs.aitranslator"
                iconFile.set(project.file("src/main/resources/icons/app-icon.icns"))
            }
        }
    }
}
