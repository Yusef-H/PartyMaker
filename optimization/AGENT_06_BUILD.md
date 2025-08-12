# AGENT 06 - Build & APK Optimization

## 🎯 Mission: Build Performance & APK Size Optimization
**Estimated Time: 3-4 hours**
**Priority: MEDIUM**

---

## 📋 Tasks Overview

### Task 1: Gradle Build Optimization
**Time: 1-2 hours | Priority: HIGH**

#### Files to Modify:
- `gradle.properties`
- `build.gradle` (Project level)
- `app/build.gradle` (Module level)
- `proguard-rules.pro`

#### 1. Update gradle.properties:
```properties
# Gradle Performance Optimization
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.daemon=true

# Android Build Optimization
android.useAndroidX=true
android.enableJetifier=true
android.enableR8.fullMode=true
android.enableBuildConfig=false
android.enableResValues=false

# Build Cache
android.enableBuildCache=true
android.experimental.cacheCompileLibResources=true

# Incremental Processing
kapt.incremental.apt=true
kapt.use.worker.api=true
kapt.include.compile.classpath=false

# Resource Processing
android.enableAapt2jni=true
android.injected.invoked.from.ide=false
```

#### 2. Update Project build.gradle:
```gradle
buildscript {
    ext {
        kotlin_version = "1.9.22"
        gradle_version = "8.2.2"
    }
    
    dependencies {
        classpath "com.android.tools.build:gradle:$gradle_version"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        
        // Build optimization plugins
        classpath 'com.google.gms:google-services:4.4.0'
        classpath 'com.google.firebase:firebase-crashlytics-gradle:2.9.9'
    }
}

// Build scan for performance analysis
if (hasProperty('buildScan')) {
    buildScan {
        termsOfServiceUrl = 'https://gradle.com/terms-of-service'
        termsOfServiceAgree = 'yes'
        publishAlways()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        
        // Add exclusions for faster dependency resolution
        all { ResolutionResult.ComponentResult result ->
            if (result.moduleVersion?.name?.startsWith("support-")) {
                result.because("Using AndroidX instead")
            }
        }
    }
}
```

#### 3. Update app/build.gradle:
```gradle
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'

android {
    compileSdk 35
    namespace 'com.example.partymaker'
    
    defaultConfig {
        applicationId "com.example.partymaker"
        minSdk 33
        targetSdk 35
        versionCode 1
        versionName "1.0"
        
        // MultiDex optimization
        multiDexEnabled true
        multiDexKeepProguard file('multidex-rules.pro')
        
        // Vector drawable optimization
        vectorDrawables.useSupportLibrary = true
        vectorDrawables.generatedDensities = ['mdpi', 'hdpi', 'xhdpi', 'xxhdpi']
        
        // Resource optimization
        resConfigs "en", "he" // Keep only needed languages
        
        // Native library optimization
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86_64' // Remove x86 for production
        }
        
        // Test runner optimization
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments clearPackageData: 'true'
    }
    
    buildTypes {
        debug {
            debuggable true
            minifyEnabled false
            shrinkResources false
            crunchPngs false // Disable PNG crunching in debug for faster builds
            
            // Disable Crashlytics in debug
            ext.enableCrashlytics = false
        }
        
        release {
            debuggable false
            minifyEnabled true
            shrinkResources true
            crunchPngs true
            
            // ProGuard/R8 configuration
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            
            // Zipalign and sign
            zipAlignEnabled true
            
            // Enable Crashlytics
            ext.enableCrashlytics = true
        }
    }
    
    // Build features optimization
    buildFeatures {
        viewBinding true
        dataBinding false // Disable if not used
        compose false     // Disable if not used
        buildConfig true
        resValues true
    }
    
    // Compile options
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
        incremental true
    }
    
    kotlinOptions {
        jvmTarget = '11'
        freeCompilerArgs += [
            '-opt-in=kotlin.RequiresOptIn',
            '-Xjvm-default=all'
        ]
    }
    
    // Packaging optimization
    packagingOptions {
        pickFirst '**/libc++_shared.so'
        pickFirst '**/libjsc.so'
        
        // Exclude unnecessary files
        excludes += [
            'META-INF/DEPENDENCIES',
            'META-INF/LICENSE',
            'META-INF/LICENSE.txt',
            'META-INF/license.txt',
            'META-INF/NOTICE',
            'META-INF/NOTICE.txt',
            'META-INF/notice.txt',
            'META-INF/ASL2.0',
            'META-INF/AL2.0',
            'META-INF/LGPL2.1',
            'META-INF/*.kotlin_module',
            '**/*.kotlin_builtins'
        ]
    }
    
    // Lint optimization
    lintOptions {
        disable 'MissingTranslation', 'ExtraTranslation'
        checkReleaseBuilds false
        abortOnError false
        quiet true
    }
    
    // Test options optimization
    testOptions {
        unitTests {
            includeAndroidResources = true
            returnDefaultValues = true
        }
        animationsDisabled true
    }
    
    // Bundle optimization
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
}

dependencies {
    // Use implementation instead of compile
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    
    // Use specific versions and avoid transitive dependencies
    implementation('androidx.appcompat:appcompat:1.6.1') {
        exclude group: 'androidx.legacy'
    }
    
    implementation('com.google.android.material:material:1.11.0') {
        exclude group: 'androidx.legacy'
    }
    
    // Room with kapt optimization
    def room_version = "2.6.1"
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"
    
    // Network dependencies
    implementation('com.squareup.retrofit2:retrofit:2.9.0') {
        exclude group: 'com.squareup.okhttp3'
    }
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Image loading optimization
    implementation('com.github.bumptech.glide:glide:4.16.0') {
        exclude group: 'com.android.support'
    }
    kapt 'com.github.bumptech.glide:compiler:4.16.0'
    
    // Use debugImplementation for debug-only dependencies
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
    debugImplementation 'com.facebook.flipper:flipper:0.182.0'
    
    // Test dependencies
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

---

### Task 2: ProGuard/R8 Optimization
**Time: 1 hour | Priority: MEDIUM-HIGH**

#### Update proguard-rules.pro:
```proguard
# Basic optimizations
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization passes
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep application class
-keep public class com.example.partymaker.PartyApplication

# Keep model classes (for JSON serialization)
-keepclassmembers class com.example.partymaker.data.model.** {
    <fields>;
    <methods>;
}

# Keep Room entities and DAOs
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class * extends androidx.room.RoomDatabase {
    <methods>;
}

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Retrofit interfaces
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# Gson optimization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Glide optimization
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}

# OkHttp optimization
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove debug code
-assumenosideeffects class com.example.partymaker.utils.infrastructure.PerformanceMonitor {
    public static void startTiming(...);
    public static void endTiming(...);
    public static void trackMemoryUsage(...);
}

# Optimize enums
-optimizations !code/simplification/enum

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
```

#### Create multidex-rules.pro:
```proguard
# Keep essential classes for MultiDex
-keep class androidx.multidex.** { *; }
-keep class com.example.partymaker.PartyApplication { *; }

# Keep essential Firebase classes for main dex
-keep class com.google.firebase.FirebaseApp { *; }
-keep class com.google.firebase.FirebaseOptions { *; }
-keep class com.google.firebase.auth.FirebaseAuth { *; }
-keep class com.google.firebase.database.FirebaseDatabase { *; }
```

---

### Task 3: Resource Optimization
**Time: 1 hour | Priority: MEDIUM**

#### 1. Create vector_drawables_optimization script:
**Create:** `scripts/optimize_resources.gradle`
```gradle
android.applicationVariants.all { variant ->
    variant.outputs.all { output ->
        // Optimize resources for release builds
        if (variant.buildType.name == 'release') {
            // Remove unused resources
            variant.mergeResourcesProvider.get().doLast {
                def resourceDir = variant.mergeResourcesProvider.get().outputDir
                
                // Remove unused drawable densities
                ['ldpi', 'tvdpi'].each { density ->
                    def densityDir = new File(resourceDir, "drawable-${density}")
                    if (densityDir.exists()) {
                        densityDir.deleteDir()
                        println "Removed unused density: ${density}"
                    }
                }
                
                // Optimize PNG files (if not using WebP)
                fileTree(resourceDir) {
                    include '**/*.png'
                }.each { file ->
                    // You can add PNG optimization here
                    println "Found PNG: ${file.name}"
                }
            }
        }
    }
}
```

#### 2. Update strings.xml optimization:
**Create build script to remove unused strings:**
```gradle
task removeUnusedStrings {
    doLast {
        // This would scan source files and remove unused string resources
        println "Checking for unused string resources..."
        
        def stringsFile = file('src/main/res/values/strings.xml')
        def sourceFiles = fileTree('src/main/java') {
            include '**/*.java'
            include '**/*.kt'
        }
        
        // Implementation would check R.string.* usage
        println "String optimization complete"
    }
}
```

#### 3. Optimize image resources:
**Create image optimization script:**
```bash
#!/bin/bash
# scripts/optimize_images.sh

# Convert PNG to WebP for better compression
find app/src/main/res -name "*.png" -not -path "*/mipmap*" | while read png; do
    webp="${png%.png}.webp"
    if [ ! -f "$webp" ]; then
        cwebp -q 80 "$png" -o "$webp"
        echo "Converted $png to WebP"
        # Remove original PNG after conversion
        # rm "$png"
    fi
done

echo "Image optimization complete"
```

---

### Task 4: Build Performance Monitoring
**Time: 0.5-1 hour | Priority: LOW-MEDIUM**

#### 1. Create BuildTimeTracker:
**Create:** `buildSrc/src/main/groovy/BuildTimeTracker.groovy`
```groovy
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildTimeTracker implements Plugin<Project> {
    void apply(Project project) {
        def startTime = System.currentTimeMillis()
        
        project.gradle.buildFinished { result ->
            def endTime = System.currentTimeMillis()
            def buildTime = (endTime - startTime) / 1000.0
            
            println "\n==== Build Performance ===="
            println "Total build time: ${buildTime}s"
            
            if (buildTime > 30) {
                println "WARNING: Build time is slow (>30s)"
            }
            
            // Log module build times
            project.subprojects.each { subproject ->
                def moduleTime = subproject.hasProperty('buildStartTime') ? 
                    (endTime - subproject.buildStartTime) / 1000.0 : 0
                if (moduleTime > 0) {
                    println "  ${subproject.name}: ${moduleTime}s"
                }
            }
            println "=========================="
        }
    }
}
```

#### 2. Create APK analysis script:
**Create:** `scripts/analyze_apk.gradle`
```gradle
task analyzeApk(type: Exec) {
    dependsOn assembleRelease
    
    def apkFile = file("${buildDir}/outputs/apk/release/app-release.apk")
    
    doLast {
        if (apkFile.exists()) {
            println "\n==== APK Analysis ===="
            
            // APK size
            def apkSize = apkFile.length() / (1024 * 1024)
            println "APK Size: ${String.format('%.2f', apkSize)} MB"
            
            if (apkSize > 50) {
                println "WARNING: APK size is large (>50MB)"
            }
            
            // Method count (requires dexcount-gradle-plugin)
            if (project.hasProperty('dexcount')) {
                println "Method count analysis available with dexcount plugin"
            }
            
            println "===================="
        }
    }
}

// Add APK analyzer plugin
apply plugin: 'com.getkeepsafe.dexcount'
```

#### 3. Update build.gradle to include monitoring:
```gradle
// Apply build time tracking
apply from: 'scripts/optimize_resources.gradle'
apply from: 'scripts/analyze_apk.gradle'

// Add dependency size analysis
configurations.all {
    resolutionStrategy {
        eachDependency { details ->
            if (details.requested.group == 'androidx.legacy') {
                details.useTarget group: 'androidx.legacy', name: 'legacy-support-v4', version: '1.0.0'
                details.because 'Resolve legacy support conflicts'
            }
        }
    }
}

// Task to analyze dependency sizes
task analyzeDependencies {
    doLast {
        println "\n==== Dependency Analysis ===="
        
        configurations.implementation.resolvedConfiguration.resolvedArtifacts.each { artifact ->
            def file = artifact.file
            if (file.exists()) {
                def sizeMB = file.length() / (1024 * 1024)
                if (sizeMB > 1) { // Only show dependencies > 1MB
                    println "${artifact.name}: ${String.format('%.2f', sizeMB)} MB"
                }
            }
        }
        
        println "=========================="
    }
}
```

---

## ✅ Testing Instructions

### Build Performance Tests:

1. **Clean Build Test:**
```bash
# Measure clean build time
./gradlew clean
time ./gradlew assembleDebug

# Should be under 2 minutes for debug builds
```

1. **Incremental Build Test:**
```bash
# Make small change and rebuild
echo "// test comment" >> app/src/main/java/MainActivity.java
time ./gradlew assembleDebug

# Should be under 30 seconds for incremental builds
```

1. **APK Size Analysis:**
```bash
# Build release APK and analyze
./gradlew assembleRelease
./gradlew analyzeApk

# Check APK size and method count
```

1. **Build Cache Test:**
```bash
# Test build cache effectiveness
./gradlew clean
./gradlew assembleDebug --build-cache --info | grep "FROM-CACHE"
```

### Expected Results:
- Clean debug build under 2 minutes
- Incremental builds under 30 seconds
- Release APK under 50MB
- Build cache hit rate > 50%

---

## 🚨 Critical Points

1. **Test Build on CI/CD**: Ensure optimizations work in automated builds
2. **Check APK Functionality**: Verify ProGuard doesn't break app functionality
3. **Monitor Build Times**: Track build performance over time
4. **Test Different Configurations**: Debug, release, and different flavors

---

## 📊 Success Criteria

- [ ] Clean build time under 2 minutes (debug)
- [ ] Incremental builds under 30 seconds
- [ ] Release APK size under 50MB
- [ ] Build cache working effectively
- [ ] ProGuard rules not breaking functionality
- [ ] Resource optimization reducing APK size

---

## 📋 Build Optimization Checklist

### Gradle Configuration:
- [ ] Parallel builds enabled
- [ ] Build cache enabled
- [ ] JVM memory optimized (4GB+)
- [ ] Incremental compilation enabled

### APK Optimization:
- [ ] Resource shrinking enabled
- [ ] Code minification enabled (R8)
- [ ] PNG optimization/WebP conversion
- [ ] Unused resource removal

### Performance Monitoring:
- [ ] Build time tracking
- [ ] APK size analysis
- [ ] Dependency size monitoring
- [ ] Method count tracking

---

**Agent 06 Priority:** Focus on Gradle optimization first - biggest build time impact!
**Time Allocation:** Gradle Config (40%) → ProGuard Rules (30%) → Resource Optimization (20%) → Monitoring (10%)