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

// Configure all projects in the build
allprojects {
    // Apply common configurations to all modules
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            // Enable Java 8 compatibility
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            
            // Use the experimental Kotlin compiler
            freeCompilerArgs.add("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }
}

// Configure Gradle's build cache
tasks.withType<Delete> {
    // Ensure clean task doesn't fail on missing files
    doFirst {
        project.fileTree(".").matching {
            include("**/*.hprof")
            include("**/*.log")
        }.forEach {
            it.delete()
        }
    }
}

// No dependencies or configuration should be added here unless it applies to ALL modules.