plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sqldelight) apply false
}

// AGPL License Header Check — enforces header presence in all .kt and .kts source files
val licenseHeaderFile = file("config/license-header.txt")
val licenseHeaderText = licenseHeaderFile.readText().trim()

val checkLicenseHeaders = tasks.register("checkLicenseHeaders") {
    group = "verification"
    description = "Checks that all Kotlin source files contain the AGPL license header."

    doLast {
        val violations = mutableListOf<String>()
        val sourceDirectories = subprojects.flatMap { subproject ->
            subproject.projectDir.resolve("src").walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
                .toList()
        }
        // Also check root .kts files (build scripts)
        val rootBuildScripts = projectDir.listFiles()
            ?.filter { it.isFile && it.extension == "kts" }
            ?.toList() ?: emptyList()

        val allFiles = sourceDirectories + rootBuildScripts

        allFiles.forEach { file ->
            val content = file.readText()
            if (!content.trimStart().startsWith(licenseHeaderText)) {
                violations.add(file.relativeTo(projectDir).path)
            }
        }

        if (violations.isNotEmpty()) {
            val message = buildString {
                appendLine("❌ AGPL license header missing or incorrect in ${violations.size} file(s):")
                appendLine()
                violations.take(20).forEach { appendLine("  • $it") }
                if (violations.size > 20) {
                    appendLine("  ... and ${violations.size - 20} more")
                }
                appendLine()
                appendLine("Every .kt and .kts source file must begin with the AGPL license header.")
                appendLine("See: config/license-header.txt")
            }
            throw GradleException(message)
        }

        logger.lifecycle("✅ License header check passed — ${allFiles.size} file(s) verified.")
    }
}

// Install Git hooks from version-controlled scripts directory
val installGitHooks = tasks.register<Copy>("installGitHooks") {
    group = "git hooks"
    description = "Installs pre-commit Git hooks for ktlint and detekt enforcement."
    from("scripts/git-hooks/pre-commit")
    into(".git/hooks")
    filePermissions {
        unix("rwxr-xr-x")
    }
}

// Auto-install hooks when the project is configured
tasks.matching { it.name == "prepareKotlinBuildScriptModel" }.configureEach {
    dependsOn(installGitHooks)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
    }

    // Make release assemble depend on ktlintCheck, detekt, AND license header check
    // Release builds FAIL on any violations
    afterEvaluate {
        tasks.matching { it.name == "assembleRelease" }.configureEach {
            dependsOn(tasks.named("ktlintCheck"))
            dependsOn(tasks.named("detekt"))
            dependsOn(rootProject.tasks.named("checkLicenseHeaders"))
        }
        // Desktop/JVM modules use 'assemble' directly (no assembleRelease variant)
        // Ensure ktlint is also wired to the general assemble for non-Android modules
        tasks.matching { it.name == "assemble" }.configureEach {
            dependsOn(tasks.named("ktlintCheck"))
        }
    }
}
