// Root build.gradle.kts – global Gradle configuration for all modules

plugins {
    // Plugin aliases are managed via libs.versions.toml for consistency
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Plugin for managing secrets (API keys, etc.)
        classpath(libs.secrets.gradle.plugin)
    }
}

// No dependencies or configuration should be added here unless it applies to ALL modules.

