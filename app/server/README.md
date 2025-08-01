# PartyMaker Server

A Spring Boot server that acts as a secure middleware between the PartyMaker Android app and Firebase, providing optimized data access and enhanced performance.

## Setup

1. Generate a Firebase Admin SDK service account key:
    - Go to the Firebase Console: https://console.firebase.google.com/
    - Select your project: `partymaker-9c966`
    - Go to Project Settings > Service accounts
    - Click "Generate new private key"
    - Save the JSON file
    - Copy the contents to `src/main/resources/firebase-service-account.json`

## Running the server

```bash
./gradlew bootRun
```

The server will start on port 8080.

## 🚀 Features & Performance Enhancements

### Server Optimizations (August 2025)
- **Enhanced Request Handling**: Optimized Firebase data retrieval with improved error handling
- **Secure Data Filtering**: Server-side user group filtering prevents client-side data exposure
- **Timeout Management**: 10-second request timeouts to prevent hanging connections
- **Memory Optimization**: Efficient data processing and response streaming
- **Error Sanitization**: Secure error responses that don't leak sensitive information

### Key Benefits
- **Reduced Client Load**: Heavy data processing done on server
- **Enhanced Security**: Centralized data access control
- **Better Performance**: Optimized Firebase queries with caching
- **Consistent API**: Unified interface for all Firebase operations

## 📋 API Endpoints

### Core Firebase Operations

#### Get All Groups
```http
GET /api/firebase/Groups
```
**Response**: JSON object with all groups and optimized user filtering

#### Get All Users
```http
GET /api/firebase/Users
```
**Response**: JSON object with all user data

#### Get All Group Messages
```http
GET /api/firebase/GroupsMessages
```
**Response**: JSON object with all group messages

### Generic Firebase Operations

#### Get Data
```http
GET /api/firebase/data/{path}
```
**Response**: JSON data from specified Firebase path

#### Save Data
```http
POST /api/firebase/data/{path}
Content-Type: application/json

{
  "key": "value",
  "data": "example"
}
```

#### Update Data
```http
PUT /api/firebase/data/{path}
Content-Type: application/json

{
  "field": "updated_value"
}
```

#### Delete Data
```http
DELETE /api/firebase/data/{path}
```

## 📱 Mobile App Integration

The PartyMaker Android app integrates with this server through the `FirebaseServerClient` class, which provides optimized data access with enhanced error handling and performance.

### Key Integration Features

#### Smart Group Loading
The server provides optimized group filtering that prevents the URL encoding issues that could occur with client-side filtering:

```java
// FirebaseServerClient.java handles the integration
public void getUserGroups(String userId, DataCallback<Map<String, Group>> callback) {
    // Server handles the filtering and returns only relevant groups
    // Prevents client-side URL encoding issues
    // Provides enhanced error handling
}
```

#### Enhanced Error Handling
- **Timeout Management**: All requests have configurable timeouts
- **Graceful Degradation**: Falls back to cached data when server is unavailable
- **Error Recovery**: Automatic retry mechanisms for transient failures

#### Performance Optimizations
- **Reduced Data Transfer**: Server filters data before sending to client
- **Optimized Queries**: Server-side Firebase query optimization
- **Caching Support**: Coordinated caching between server and client

### Configuration

The Android app automatically uses the production server URL:
```java
// MainActivity.java - Force server URL
String renderUrl = "https://partymaker.onrender.com";
```

For local development, update the server URL in the app settings:
- **Emulator**: `http://10.0.2.2:8080`
- **Physical device**: `http://[your-computer-ip]:8080`

### Latest Improvements (August 2025)
- **Race Condition Prevention**: Fixed client-server race conditions in group loading
- **Enhanced Caching**: Smart cache invalidation with force refresh support
- **Better Error Messages**: More user-friendly error handling
- **Timeout Optimization**: Balanced timeouts for better user experience

## 🛠️ Technology Stack

- **Framework**: Spring Boot 2.7.5
- **Language**: Java 11
- **Build Tool**: Gradle
- **Database**: Firebase Realtime Database (via Admin SDK)
- **Authentication**: Firebase Admin SDK
- **Deployment**: Render.com (Production)
- **Development**: Local with hot reload

## 🚀 Deployment

### Production Deployment (Render.com)
The server is deployed at: `https://partymaker.onrender.com`

1. **Automatic Deployment**: Connected to GitHub repository
2. **Environment Variables**: Configure Firebase service account in Render dashboard
3. **Health Checks**: Automatic health monitoring and restart
4. **SSL**: Automatic HTTPS with certificate management

### Local Development
```bash
# Start local server
./gradlew bootRun

# Server runs on http://localhost:8080
# Update Android app to use local server for testing
```

### Build for Production
```bash
# Build JAR file
./gradlew bootJar

# JAR file location: build/libs/server-*.jar
# Deploy to your preferred cloud platform
```

## 🔧 Configuration

### Required Environment Variables
- `FIREBASE_SERVICE_ACCOUNT`: Firebase Admin SDK service account JSON
- `FIREBASE_PROJECT_ID`: Your Firebase project ID

### Optional Configuration
- `SERVER_PORT`: Server port (default: 8080)
- `FIREBASE_DATABASE_URL`: Firebase database URL
- `CORS_ALLOWED_ORIGINS`: Allowed CORS origins

## 📊 Monitoring & Health

The server provides health check endpoints:
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information

## 🔒 Security Features

- **CORS Configuration**: Properly configured cross-origin requests
- **Input Validation**: Request validation and sanitization  
- **Error Handling**: Secure error responses without information leakage
- **Timeout Protection**: Request timeouts to prevent DoS attacks
- **Firebase Security**: Server-side Firebase Admin SDK access 