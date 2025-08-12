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
        // Use Google's Maven repository first for Android dependencies
        google()
        // Use Maven Central for other dependencies  
        mavenCentral()
        // Use Gradle Plugin Portal for Gradle plugins
        gradlePluginPortal()
    }
    dependencies {
        // Plugin for managing secrets (API keys, etc.)
        classpath(libs.secrets.gradle.plugin)
        
        // Build optimization plugins
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:4.0.0")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.52.0")
    }
}

// Configure all projects in the build
allprojects {

    // Apply common configurations to all modules
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            // Enable Java 11 compatibility 
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)

            // Kotlin compiler optimizations
            freeCompilerArgs.addAll(listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-Xuse-k2" // Use K2 compiler for better performance
            ))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
        
        // Java compiler optimizations
        options.apply {
            isIncremental = true
            isFork = true
            compilerArgs.addAll(listOf(
                "-Xlint:none", // Disable lint warnings for faster compilation
                "-nowarn" // Suppress warnings
            ))
        }
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

// Build performance monitoring (configuration cache compatible)
tasks.register("buildTimeReport") {
    group = "build performance"
    description = "Reports build performance metrics"
    
    doLast {
        println("Build performance monitoring available")
        println("Use --scan flag for detailed build insights")
    }
}

// No dependencies or configuration should be added here unless it applies to ALL modules.