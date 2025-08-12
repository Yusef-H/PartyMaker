# PartyMaker Optimization Scripts

This directory contains Gradle scripts to help analyze and optimize the PartyMaker application.

## Available Scripts

### 1. analyze_apk.gradle
Provides detailed analysis of APK size and contents.

**Tasks:**
- `analyzeApk` - Analyzes APK size, method count, and content breakdown
- `analyzeProguard` - Checks ProGuard/R8 optimization effectiveness
- `findLargeResources` - Identifies resource files larger than 100KB

**Usage:**
```bash
./gradlew analyzeApk
./gradlew analyzeProguard
./gradlew findLargeResources
```

### 2. optimize_resources.gradle
Helps identify optimization opportunities for images and resources.

**Tasks:**
- `optimizeImages` - Analyzes image resources and flags oversized images
- `findUnusedResources` - Identifies potentially unused layout files
- `generateDrawableDensityReport` - Reports on drawable density coverage
- `optimizeGradle` - Suggests Gradle build optimizations
- `runAllOptimizations` - Runs all optimization checks

**Usage:**
```bash
./gradlew optimizeImages
./gradlew findUnusedResources
./gradlew generateDrawableDensityReport
./gradlew optimizeGradle

# Run all optimization checks
./gradlew runAllOptimizations
```

## How to Use

1. Apply the scripts in your app's `build.gradle.kts`:
```kotlin
apply(from = "../scripts/analyze_apk.gradle")
apply(from = "../scripts/optimize_resources.gradle")
```

2. Run the desired task from the project root:
```bash
./gradlew <taskName>
```

## Benefits

- **APK Size Reduction**: Identify large resources and unused files
- **Performance**: Find optimization opportunities in build configuration
- **Quality**: Ensure proper resource density coverage
- **Monitoring**: Track APK size and method count over time

## Tips

- Run `analyzeApk` after each release build to monitor size trends
- Use `findUnusedResources` before releases to clean up unused files
- Check `generateDrawableDensityReport` to ensure all screen densities are supported
- Review `optimizeGradle` suggestions to improve build times

## Requirements

- Android SDK tools (aapt, etc.)
- Gradle 7.0+
- Java 11+

## Output

All tasks print their results to the console. For detailed analysis, redirect output to a file:
```bash
./gradlew analyzeApk > apk-analysis.txt
```