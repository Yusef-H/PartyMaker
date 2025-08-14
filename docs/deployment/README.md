# Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Environment Setup](#environment-setup)
4. [Backend Deployment](#backend-deployment)
5. [Android App Deployment](#android-app-deployment)
6. [CI/CD Pipeline](#cicd-pipeline)
7. [Monitoring Setup](#monitoring-setup)
8. [Troubleshooting](#troubleshooting)

## Overview

This guide covers the complete deployment process for the PartyMaker application, including backend services, Android application, and supporting infrastructure.

## Prerequisites

### Required Tools
- Java JDK 11+
- Android Studio Arctic Fox or later
- Docker & Docker Compose
- Git
- Firebase CLI
- Google Cloud SDK (optional)
- Node.js 14+ (for Firebase tools)

### Required Accounts
- Firebase Project with billing enabled
- Google Play Console Developer Account
- GitHub account (for CI/CD)
- Render.com account (or alternative cloud provider)
- Domain name (optional but recommended)

## Environment Setup

### 1. Firebase Configuration

#### Create Firebase Project
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Create new project
firebase projects:create partymaker-prod
```

#### Enable Firebase Services
1. Navigate to [Firebase Console](https://console.firebase.google.com)
2. Enable the following services:
   - Authentication (Email/Password, Google Sign-In)
   - Realtime Database
   - Cloud Storage
   - Cloud Messaging

#### Generate Service Account
1. Go to Project Settings → Service Accounts
2. Generate new private key
3. Save as `firebase-service-account.json`

### 2. Configuration Files

#### Backend Configuration
Create `application.properties`:
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Firebase Configuration
firebase.database.url=https://partymaker-prod.firebaseio.com
firebase.storage.bucket=partymaker-prod.appspot.com
firebase.project.id=partymaker-prod

# Security
security.jwt.secret=${JWT_SECRET}
security.cors.allowed-origins=https://partymaker.com

# Monitoring
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always

# Logging
logging.level.root=INFO
logging.level.com.example.partymaker=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

#### Android Configuration
Create `secrets.properties`:
```properties
# API Keys
MAPS_API_KEY=your_google_maps_api_key
OPENAI_API_KEY=your_openai_api_key

# Server Configuration
SERVER_URL_PROD=https://partymaker.onrender.com
SERVER_URL_DEV=http://10.0.2.2:8080

# Feature Flags
ENABLE_CRASHLYTICS=true
ENABLE_ANALYTICS=true
ENABLE_DEBUG_LOGGING=false
```

## Backend Deployment

### Option 1: Deploy to Render.com

#### 1. Prepare Dockerfile
```dockerfile
FROM openjdk:11-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY src/main/resources/firebase-service-account.json firebase-service-account.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-service-account.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. Create render.yaml
```yaml
services:
  - type: web
    name: partymaker-api
    env: docker
    repo: https://github.com/yourusername/partymaker
    branch: main
    dockerfilePath: ./app/server/Dockerfile
    dockerContext: ./app/server
    envVars:
      - key: JAVA_OPTS
        value: -Xmx512m -Xms256m
      - key: FIREBASE_CONFIG
        sync: false
    healthCheckPath: /api/actuator/health
    autoDeploy: true
```

#### 3. Deploy
```bash
# Build the application
cd app/server
./gradlew clean build

# Push to GitHub
git add .
git commit -m "Deploy backend to Render"
git push origin main

# Render will automatically deploy
```

### Option 2: Deploy to Google Cloud Run

#### 1. Build and Push Docker Image
```bash
# Build Docker image
docker build -t gcr.io/partymaker-prod/api:latest ./app/server

# Push to Google Container Registry
docker push gcr.io/partymaker-prod/api:latest
```

#### 2. Deploy to Cloud Run
```bash
gcloud run deploy partymaker-api \
  --image gcr.io/partymaker-prod/api:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-service-account.json"
```

### Option 3: Deploy to AWS ECS

#### 1. Create Task Definition
```json
{
  "family": "partymaker-api",
  "taskRoleArn": "arn:aws:iam::account-id:role/ecsTaskRole",
  "executionRoleArn": "arn:aws:iam::account-id:role/ecsExecutionRole",
  "networkMode": "awsvpc",
  "containerDefinitions": [
    {
      "name": "api",
      "image": "your-ecr-uri/partymaker-api:latest",
      "memory": 1024,
      "cpu": 512,
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "production"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/partymaker-api",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "api"
        }
      }
    }
  ],
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024"
}
```

## Android App Deployment

### 1. Prepare for Release

#### Update Version
Edit `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        versionCode = 2
        versionName = "1.1.0"
    }
}
```

#### Configure ProGuard
```proguard
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep model classes
-keep class com.example.partymaker.data.model.** { *; }

# Keep Retrofit interfaces
-keep interface com.example.partymaker.data.api.** { *; }
```

### 2. Build Release APK/Bundle

#### Generate Signed Bundle
```bash
# Clean build
./gradlew clean

# Build release bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

#### Sign the Bundle
1. Create keystore (first time only):
```bash
keytool -genkey -v -keystore partymaker-release.keystore \
  -alias partymaker -keyalg RSA -keysize 2048 -validity 10000
```

2. Configure signing in `build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../partymaker-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "partymaker"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3. Deploy to Google Play Store

#### Prepare Store Listing
1. App title and description
2. Screenshots (phone, tablet, wear)
3. Feature graphic (1024x500)
4. App icon (512x512)
5. Privacy policy URL
6. App category and content rating

#### Upload to Play Console
```bash
# Using fastlane (optional)
fastlane supply --aab app/build/outputs/bundle/release/app-release.aab
```

Or manually:
1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app
3. Go to Release > Production
4. Create new release
5. Upload AAB file
6. Fill release notes
7. Review and roll out

### 4. Alternative Distribution

#### Firebase App Distribution
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Upload to Firebase App Distribution
firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
  --app YOUR_FIREBASE_APP_ID \
  --groups "qa-team, beta-testers" \
  --release-notes "Bug fixes and performance improvements"
```

## CI/CD Pipeline

### GitHub Actions Configuration

Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy PartyMaker

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Run backend tests
        working-directory: ./app/server
        run: ./gradlew test
      
      - name: Run Android tests
        run: ./gradlew test

  build-backend:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        working-directory: ./app/server
        run: |
          docker build -t partymaker-api:${{ github.sha }} .
          docker tag partymaker-api:${{ github.sha }} partymaker-api:latest
      
      - name: Push to registry
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push partymaker-api:${{ github.sha }}
          docker push partymaker-api:latest

  build-android:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Decode keystore
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > partymaker-release.keystore
      
      - name: Build release APK
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: release-apk
          path: app/build/outputs/apk/release/app-release.apk

  deploy:
    needs: [build-backend, build-android]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to production
        run: |
          # Trigger deployment webhook or API call
          curl -X POST ${{ secrets.DEPLOY_WEBHOOK_URL }}
```

## Monitoring Setup

### 1. Application Performance Monitoring

#### Firebase Performance
```java
// Add to PartyApplication.java
FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);

// Custom traces
Trace myTrace = FirebasePerformance.getInstance().newTrace("api_call");
myTrace.start();
// ... perform operation
myTrace.stop();
```

#### Custom Metrics
```java
public class PerformanceMonitor {
    public static void logMetric(String name, long value) {
        FirebasePerformance.getInstance()
            .newTrace(name)
            .putMetric("duration", value)
            .stop();
    }
}
```

### 2. Error Tracking

#### Firebase Crashlytics
```kotlin
// Initialize in Application class
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

// Log custom events
FirebaseCrashlytics.getInstance().log("User clicked party creation")

// Record non-fatal exceptions
try {
    // risky operation
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
}
```

### 3. Server Monitoring

#### Health Checks
```java
@RestController
public class HealthController {
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("database", checkDatabaseHealth());
        status.put("firebase", checkFirebaseHealth());
        return ResponseEntity.ok(status);
    }
}
```

#### Prometheus Metrics
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'partymaker-api'
    static_configs:
      - targets: ['api.partymaker.com:8080']
    metrics_path: '/actuator/prometheus'
```

### 4. Alerting

#### Configure Alerts
```yaml
# alertmanager.yml
route:
  receiver: 'team-notifications'
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty'
    - match:
        severity: warning
      receiver: 'slack'

receivers:
  - name: 'team-notifications'
    email_configs:
      - to: 'team@partymaker.com'
  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_KEY'
  - name: 'slack'
    slack_configs:
      - api_url: 'YOUR_SLACK_WEBHOOK_URL'
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Backend Won't Start
```bash
# Check logs
docker logs partymaker-api

# Common fixes:
# - Verify Firebase credentials are correct
# - Check environment variables are set
# - Ensure port 8080 is not in use
```

#### 2. Android Build Failures
```bash
# Clean and rebuild
./gradlew clean
./gradlew --stop
./gradlew assembleDebug --stacktrace

# Common fixes:
# - Update Gradle wrapper version
# - Clear Gradle cache: rm -rf ~/.gradle/caches/
# - Sync project in Android Studio
```

#### 3. Firebase Connection Issues
```bash
# Verify Firebase configuration
firebase projects:list

# Test Firebase connection
curl -X GET "https://partymaker-prod.firebaseio.com/.json"

# Common fixes:
# - Regenerate google-services.json
# - Check Firebase security rules
# - Verify service account permissions
```

#### 4. Play Store Upload Issues
- Ensure version code is incremented
- Check signing configuration
- Verify all required assets are provided
- Review content rating questionnaire

### Performance Optimization

#### Backend Optimization
```bash
# JVM tuning
java -Xmx1g -Xms512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar app.jar
```

#### Android Optimization
```kotlin
// Enable R8 optimizer
android {
    buildTypes {
        release {
            minifyEnabled = true
            shrinkResources = true
            proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'))
        }
    }
}
```

## Rollback Procedures

### Backend Rollback
```bash
# Render.com
# Navigate to dashboard → Select previous deployment → Promote to production

# Docker
docker pull partymaker-api:previous-version
docker stop partymaker-api
docker run -d --name partymaker-api partymaker-api:previous-version

# Kubernetes
kubectl rollout undo deployment/partymaker-api
```

### Android Rollback
1. Go to Play Console → Release management
2. Select the problematic release
3. Click "Halt rollout"
4. Create new release with previous APK

## Security Checklist

- [ ] All secrets in environment variables
- [ ] SSL certificates valid and not expiring
- [ ] Firebase security rules reviewed
- [ ] API rate limiting enabled
- [ ] ProGuard/R8 obfuscation enabled
- [ ] Debug logging disabled in production
- [ ] Sensitive data encrypted
- [ ] Security headers configured
- [ ] Regular dependency updates
- [ ] Penetration testing completed

## Support

For deployment support:
- Email: devops@partymaker.com
- Slack: #partymaker-deployment
- Documentation: [Internal Wiki](https://wiki.partymaker.com/deployment)

---

*Deployment Guide Version: 1.0.0 | Last Updated: August 2025*