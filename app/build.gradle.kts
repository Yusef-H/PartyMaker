// app/build.gradle.kts – Android application module configuration

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.diffplug.spotless") version "7.2.1"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.getkeepsafe.dexcount") version "4.0.0" // For APK analysis
}

android {
    namespace = "com.example.partymaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.partymaker"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = null // Disable Android instrumentation tests
        
        // MultiDex optimization
        multiDexEnabled = true
        multiDexKeepProguard = file("multidex-rules.pro")
        
        // Vector drawable optimization
        vectorDrawables {
            useSupportLibrary = true
            generatedDensities("mdpi", "hdpi", "xhdpi", "xxhdpi")
        }
        
        // Resource configuration - keep only needed languages
        // Using the new androidResources API instead of deprecated resourceConfigurations
        
        // Native library optimization - remove x86 for production builds
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }

        // Load API keys from local.properties OR secrets.properties
        val localPropertiesFile = rootProject.file("local.properties")
        val secretsPropertiesFile = rootProject.file("secrets.properties")

        val properties = Properties()

        // Try loading from local.properties first
        if (localPropertiesFile.exists()) {
            localPropertiesFile.reader().use { properties.load(it) }
        }

        // Also load from secrets.properties (will override if both exist)
        if (secretsPropertiesFile.exists()) {
            secretsPropertiesFile.reader().use { properties.load(it) }
        }

        // API Keys - Can be in either local.properties or secrets.properties
        val openAiKey =
            properties.getProperty("openai.api.key")
                ?: properties.getProperty("OPENAI_API_KEY")
                ?: System.getenv("OPENAI_API_KEY")
                ?: ""

        val mapsKey =
            properties.getProperty("maps.api.key")
                ?: properties.getProperty("MAPS_API_KEY")
                ?: System.getenv("MAPS_API_KEY")
                ?: ""

        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey.ifEmpty { "YOUR_API_KEY_HERE" }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            isPseudoLocalesEnabled = false // Disable for faster builds
            isCrunchPngs = false // Disable PNG crunching in debug for faster builds
            
            // Disable Crashlytics in debug builds 
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            isPseudoLocalesEnabled = false
            isCrunchPngs = true // Enable PNG optimization in release
            
            // Enable Crashlytics in release builds
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "true")
            
            // ProGuard/R8 configuration with optimization
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            
            // Zip align is enabled by default in modern AGP
            // isZipAlignEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        // Kotlin compiler optimizations
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        // Disable unused features for faster builds
        dataBinding = false
        mlModelBinding = false
        prefab = false
        renderScript = false
        shaders = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }
    
    // Resource optimization - keep only needed languages
    androidResources {
        localeFilters += listOf("en", "he")
    }
    
    // Packaging optimization
    packaging {
        resources {
            // Pick first occurrence of duplicate files
            pickFirsts += listOf(
                "**/libc++_shared.so",
                "**/libjsc.so",
                "META-INF/DEPENDENCIES"
            )
            
            // Exclude unnecessary files to reduce APK size
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt", 
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "**/*.kotlin_builtins",
                "**/kotlin/**",
                "kotlin-tooling-metadata.json"
            )
        }
    }
    
    // Bundle optimization for App Bundle builds  
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    // Disable all test options
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = false
        unitTests.all { it.enabled = false }
    }

    // Disable lint checks
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Disable all test and lint tasks
tasks.configureEach {
    if (
        name.contains("test", ignoreCase = true) ||
        name.contains("androidTest", ignoreCase = true) ||
        name.contains("lint", ignoreCase = true)
    ) {
        enabled = false
    }
}

// Custom build task that skips tests
tasks.register("buildWithoutTests") {
    group = "build"
    description = "Assembles the project without running tests"
    dependsOn("assemble")
}

// Override the default build task
tasks.named("build") {
    dependsOn("buildWithoutTests")
    setDependsOn(
        dependsOn.filterNot {
            it.toString().contains("test", ignoreCase = true) ||
                it.toString().contains("androidTest", ignoreCase = true) ||
                it.toString().contains("lint", ignoreCase = true)
        },
    )
}

// Spotless – Code formatting for Java, Kotlin and XML
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.17.0") // required for JVM 21/24+
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Kotlin support
    kotlin {
        target("src/**/*.kt")
        ktlint("1.2.1")
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Kotlin Gradle scripts (build.gradle.kts, etc.)
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt")
        ktlint("1.2.1")
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Format XML layout/resources
    format("xml") {
        target("src/**/*.xml")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    // --- AndroidX Core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) {
        exclude(group = "androidx.legacy", module = "legacy-support-v4")
    }
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.activity.compose)
    
    // --- Image Processing ---
    implementation("androidx.exifinterface:exifinterface:1.4.1")

    // --- Lifecycle & ViewModel ---
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Material Design (classic) ---
    implementation(libs.material)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.storage)
    implementation(libs.firebase.firestore)
    // Note: Firebase Cloud Messaging intentionally excluded

    // --- Google Services ---
    implementation(libs.gms.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)

    // --- Room Database ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    annotationProcessor(libs.androidx.room.compiler)

    // --- Media & Image Loading ---
    implementation(libs.picasso)
    implementation(libs.circleimageview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // --- Animations ---
    implementation(libs.lottie)
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // --- OpenAI & Networking ---
    implementation(libs.openai.client)
    implementation(libs.ktor.client.android)
    implementation(libs.okhttp)
    implementation(libs.json)

    // --- Gson JSON Parser ---
    implementation(libs.gson)

    // --- Concurrent Programming ---
    implementation(libs.androidx.concurrent.futures)

    // --- Security ---
    implementation(libs.androidx.security.crypto)

    // --- Memory Leak Detection (Debug Only) ---
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

// Secrets plugin configuration
secrets {
    defaultPropertiesFileName = "secrets.properties"
}

// Build optimization scripts available:
// Run: ./gradlew generateResourceOptimizationReport
// Run: ./gradlew fullApkAnalysis

// ===== BUILD OPTIMIZATION & ANALYSIS TASKS =====

// APK Size Analysis Task
tasks.register("analyzeApk") {
    group = "build analysis"
    description = "Analyzes APK size and provides optimization suggestions"
    dependsOn("assembleRelease")
    
    doLast {
        val apkFile = file("${layout.buildDirectory.get()}/outputs/apk/release/app-release.apk")
        
        if (apkFile.exists()) {
            val apkSizeMB = apkFile.length() / (1024.0 * 1024.0)
            
            println("\n==== PartyMaker APK Analysis ====")
            println("APK Size: ${String.format("%.2f", apkSizeMB)} MB")
            
            when {
                apkSizeMB < 25 -> println("✓ APK size is excellent (<25MB)")
                apkSizeMB < 50 -> println("✓ APK size is good (25-50MB)")
                apkSizeMB < 100 -> println("⚠ APK size is acceptable (50-100MB)")
                else -> println("❌ APK size is large (>100MB) - consider optimization")
            }
            
            println("Recommend using App Bundle for smaller downloads")
            println("==============================\n")
        } else {
            println("❌ Release APK not found. Run 'assembleRelease' first.")
        }
    }
}

// Dependency Size Analysis Task  
tasks.register("analyzeDependencies") {
    group = "build analysis"
    description = "Analyzes dependency sizes"
    
    doLast {
        println("\n==== Dependency Size Analysis ====")
        
        configurations.getByName("implementation").resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val file = artifact.file
            if (file.exists()) {
                val sizeMB = file.length() / (1024.0 * 1024.0)
                if (sizeMB > 1.0) {
                    println("${artifact.name}: ${String.format("%.2f", sizeMB)} MB")
                }
            }
        }
        
        println("==================================\n")
    }
}

// Build Cache Analysis
tasks.register("analyzeBuildCache") {
    group = "build analysis"
    description = "Provides build cache statistics"
    
    doLast {
        println("\n==== Build Cache Analysis ====")
        println("Gradle User Home: ${gradle.gradleUserHomeDir}")
        
        val cacheDir = file("${gradle.gradleUserHomeDir}/caches")
        if (cacheDir.exists()) {
            val cacheSizeGB = cacheDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum() / (1024.0 * 1024.0 * 1024.0)
            
            println("Cache Size: ${String.format("%.2f", cacheSizeGB)} GB")
            
            if (cacheSizeGB > 5.0) {
                println("⚠ Consider cleaning cache: ./gradlew cleanBuildCache")
            }
        }
        
        println("==============================\n")
    }
}
