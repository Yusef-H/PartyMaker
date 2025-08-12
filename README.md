![App Showcase](app/src/main/res/drawable/app_showcase.png)
<p align="center">
  <strong>Intelligent event planning and management solution for modern organizations</strong><br>
  make every celebration, gathering, and occasion effortlessly memorable
</p>
<p align="center">
  <img src="https://komarev.com/ghpvc/?username=Natifishman&repo=PartyMaker&color=blue&style=flat-square" alt="Project Views">
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
  <a href="https://openjdk.java.net/projects/jdk/11/"><img src="https://img.shields.io/badge/Java-11-blue" alt="Java Version"></a>
  <a href="https://android-arsenal.com/api?level=33"><img src="https://img.shields.io/badge/API-33%2B-brightgreen.svg?style=flat" alt="Android API"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-2.7.5-brightgreen.svg" alt="Spring Boot"></a>
</p>

##
**PartyMaker** is the smart way to plan any event. Our platform thinks for you - managing invites, coordinating schedules, and keeping everyone connected - so you spend less time organizing and more time celebrating. It's event planning that works.

## Archirecture
### System Architecture Overview

PartyMaker follows a **multi-tier enterprise architecture** with clear separation of concerns and scalable design patterns:

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLIENT TIER                                 │
├─────────────────────────────────────────────────────────────────┤
│  • Android Native Client (MVVM + Repository Pattern)            │
│  • UI Layer (Activities, Fragments, Adapters)                   │
│  • Presentation Layer (ViewModels, LiveData)                    │
│  • Domain Layer (Use Cases, Business Logic)                     │
│  • Data Layer (Repositories, Data Sources)                      │
└─────────────────────────────────────────────────────────────────┘
                                 ↕ HTTPS/REST
┌─────────────────────────────────────────────────────────────────┐
│                   APPLICATION TIER                              │
├─────────────────────────────────────────────────────────────────┤
│  • Spring Boot Server (MVC Pattern)                             │
│  • Controller Layer (REST Controllers)                          │
│  • Service Layer (Business Logic)                               │
│  • Configuration Layer (Firebase Admin, Security)               │
└─────────────────────────────────────────────────────────────────┘
                                 ↕ Firebase SDK
┌─────────────────────────────────────────────────────────────────┐
│                      DATA TIER                                  │
├─────────────────────────────────────────────────────────────────┤
│  • Firebase Ecosystem                                           │
│  • Authentication Service (User Management)                     │
│  • Realtime Database (NoSQL Data Storage)                       │
│  • Cloud Storage (File & Image Management)                      │
│  • Local Storage (Room Database - Offline Support)              │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Principles

- **🔄 Clean Architecture**: Clear separation between UI, business logic, and data layers
- **📊 MVVM Pattern**: Reactive programming with LiveData and ViewModels
- **🏪 Repository Pattern**: Centralized data access with multiple data sources
- **🔌 Dependency Injection**: Singleton pattern for service management
- **🔒 Security-First Design**: Authentication, encryption, and secure data handling
- **⚡ Performance Optimization**: Smart caching, lazy loading, and memory management
- **🌐 Offline-First Strategy**: Local data persistence with sync capabilities

## 🛠️ Technology Stack

### 📱 Android Client (Native)

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Language** | Java + Kotlin | Java 11, Kotlin 2.0.21 | Core development languages |
| **SDK** | Android SDK | Min 33, Target 35 | Platform compatibility |
| **Architecture** | MVVM + Repository | - | Scalable app architecture |
| **UI Framework** | Android Views + Material Design | Material 1.12.0 | Modern UI components |
| **Navigation** | Custom Navigation Manager | - | Centralized navigation logic |
| **Database** | Room + SQLite | Room 2.7.2 | Local data persistence |
| **Networking** | OkHttp + Custom Clients | OkHttp 5.1.0 | HTTP communication |
| **Image Loading** | Glide | 4.15.1 | Optimized image management |
| **Threading** | Custom ThreadUtils | - | Background task management |
| **Security** | AndroidX Security Crypto | 1.1.0-alpha06 | Encrypted storage |
| **DI Container** | Custom Singleton Pattern | - | Dependency management |

### 🖥️ Spring Boot Server

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Framework** | Spring Boot | 2.7.5 | Web application framework |
| **Language** | Java | 11 | Server-side development |
| **Architecture** | MVC + Service Layer | - | Clean server architecture |
| **Validation** | Spring Boot Validation | - | Input validation & security |
| **Database Integration** | Firebase Admin SDK | 9.1.1 | Firebase backend integration |
| **API Design** | RESTful Web Services | - | Standard API architecture |
| **Build System** | Gradle | - | Project build management |

### 🔥 Firebase Backend Services

| Service | Purpose | Features |
|---------|---------|----------|
| **Authentication** | User management | Email/password, Google Sign-In, session management |
| **Realtime Database** | NoSQL data storage | Real-time synchronization, offline support |
| **Cloud Storage** | File management | Image uploads, profile pictures, group media |
| **Security Rules** | Data access control | Role-based permissions, data validation |

### 🤖 External Integrations

| Service | Purpose | Integration |
|---------|---------|-------------|
| **OpenAI GPT** | AI chatbot assistance | Smart event planning suggestions |
| **Google Maps** | Location services | Event location mapping and discovery |
| **Google Places** | Venue discovery | Location search and autocomplete |
| **WhatsApp** | Social sharing | Direct party invitations |
| **Facebook** | Social integration | Event sharing and promotion |

## ✨ Core Features & Capabilities

### 🔐 Advanced User Management

#### Authentication & Security
- **Multi-provider Authentication**: Email/password, Google Sign-In with Firebase Auth
- **Secure Session Management**: JWT tokens with automatic refresh
- **Encrypted Local Storage**: AndroidX Security Crypto for sensitive data
- **Password Validation**: Strong password policies with custom validator
- **Biometric Authentication**: Fingerprint and face unlock support (planned)

#### Profile Management
- **Rich User Profiles**: Comprehensive user information management
- **Profile Image Upload**: Optimized image compression and storage
- **Privacy Controls**: Granular privacy settings for user data
- **Friend System**: Add, remove, and manage social connections
- **User Discovery**: Browse and connect with other platform users

### 👥 Sophisticated Group Management

#### Group Creation & Administration
- **Advanced Group Types**: Public, private, and invite-only groups
- **Role-based Permissions**: Admin, moderator, and member roles
- **Group Customization**: Custom themes, images, and branding
- **Membership Controls**: Invitation management and member moderation
- **Group Analytics**: Engagement metrics and activity tracking

#### Real-time Collaboration
- **Live Group Updates**: Real-time member activities and notifications
- **Smart Refresh Logic**: Pull-to-refresh with intelligent caching
- **Offline Group Access**: Local caching with sync when online
- **Conflict Resolution**: Automatic handling of concurrent updates

### 🎊 Comprehensive Event Planning

#### Event Creation & Management
- **Rich Event Details**: Date, time, location, description, and media
- **Multi-date Events**: Support for recurring and multi-day events
- **Location Integration**: Google Maps integration for venue selection
- **Capacity Management**: Attendee limits and waiting lists
- **Event Templates**: Pre-configured event types for quick setup

#### Invitation & RSVP System
- **Smart Invitations**: Automated invitation management
- **RSVP Tracking**: Real-time attendance monitoring
- **Reminder System**: Automated event reminders and notifications
- **Guest Management**: Plus-one handling and dietary restrictions

### 💬 Advanced Communication System

#### Real-time Messaging
- **Group Chat**: Instant messaging within event groups
- **Message History**: Persistent conversation storage with Room database
- **Rich Media Support**: Image, video, and file sharing
- **Message Encryption**: End-to-end encryption for sensitive communications
- **Offline Messaging**: Queue messages for delivery when online

#### AI-Powered Assistance
- **GPT Chatbot Integration**: Intelligent event planning suggestions
- **Natural Language Processing**: Smart command interpretation
- **Context-aware Responses**: Personalized recommendations based on user history
- **Multi-language Support**: Localized AI responses (planned)

### 🗺️ Location & Mapping Services

#### Interactive Maps
- **Google Maps Integration**: Full-featured mapping capabilities
- **Venue Discovery**: Google Places API for location search
- **GPS Navigation**: Direct navigation to event locations
- **Location Sharing**: Real-time location sharing for attendees
- **Geofencing**: Location-based notifications and check-ins

### 🔔 Intelligent Notification System

#### Multi-channel Notifications
- **Push Notifications**: Real-time event updates and messages
- **Email Notifications**: Detailed event summaries and reminders
- **SMS Integration**: Critical event updates via text messaging
- **In-app Notifications**: Rich notification center with actions
- **Smart Scheduling**: Optimal notification timing based on user behavior

#### Notification Management
- **Granular Controls**: Per-event and per-category notification settings
- **Do Not Disturb**: Automatic quiet hours and user preferences
- **Priority Notifications**: Critical updates bypass user settings
- **Notification Analytics**: Delivery and engagement tracking

### 🔄 Advanced Sharing Capabilities

#### Multi-platform Sharing
- **Social Media Integration**: Direct sharing to Facebook, Instagram, Twitter
- **Messaging Apps**: WhatsApp, Telegram, and SMS sharing
- **Email Invitations**: Rich HTML email templates
- **Deep Linking**: Direct app navigation from shared links
- **QR Code Generation**: Quick event access via QR codes

#### Content Optimization
- **Dynamic Content**: Personalized sharing content based on recipient
- **Rich Media Previews**: Attractive link previews on social platforms
- **Tracking Analytics**: Share performance and conversion metrics
- **A/B Testing**: Optimized sharing content for better engagement

## 📁 Detailed Project Structure

### Android Application Architecture

```
app/src/main/java/com/example/partymaker/
├── 🏠 PartyApplication.java                   # Application entry point
├── data/                                      # Data Layer
│   ├── api/                                   # Network & API clients
│   │   ├── AppNetworkError.java               # Custom error handling
│   │   ├── ConnectivityManager.java           # Network connectivity
│   │   ├── FirebaseServerClient.java          # Primary server client
│   │   ├── NetworkManager.java                # Network state management
│   │   ├── NetworkUtils.java                  # Network utilities
│   │   ├── OpenAiApi.java                     # OpenAI integration
│   │   └── Result.java                        # API result wrapper
│   ├── firebase/                              # Firebase integration
│   │   ├── DBRef.java                         # Firebase references
│   │   ├── FirebaseAccessManager.java         # Direct Firebase access
│   │   └── ServerDBRef.java                   # Server-mediated access
│   ├── local/                                 # Local database (Room)
│   │   ├── AppDatabase.java                   # Room database
│   │   ├── ChatMessageDao.java                # Message data access
│   │   ├── Converters.java                    # Type converters
│   │   ├── DatabaseMigrations.java            # Schema migrations
│   │   ├── GroupDao.java                      # Group data access
│   │   └── UserDao.java                       # User data access
│   ├── model/                                 # Data models
│   │   ├── ChatMessage.java                   # Chat message model
│   │   ├── ChatMessageGpt.java                # GPT message model
│   │   ├── Group.java                         # Group/party model
│   │   └── User.java                          # User model
│   └── repository/                            # Repository pattern
│       ├── DataSource.java                    # Data source interface
│       ├── GroupRepository.java               # Group data repository
│       ├── LocalGroupDataSource.java          # Local data source
│       ├── RemoteGroupDataSource.java         # Remote data source
│       └── UserRepository.java                # User data repository
├── ui/                                        # Presentation Layer
│   ├── adapters/                              # RecyclerView adapters
│   │   ├── ChatAdapter.java                   # Chat message adapter
│   │   ├── ChatbotAdapter.java                # Chatbot conversation
│   │   ├── GroupAdapter.java                  # Group list adapter
│   │   ├── InvitedAdapter.java                # Invitation adapter
│   │   ├── OptimizedRecyclerAdapter.java      # Performance-optimized adapter
│   │   └── UserAdapter.java                   # User list adapter
│   └── features/                              # Feature-based UI organization
│       ├── auth/                              # Authentication screens
│       │   ├── IntroActivity.java             # App introduction
│       │   ├── LoginActivity.java             # User login
│       │   ├── RegisterActivity.java          # User registration
│       │   └── ResetPasswordActivity.java     # Password reset
│       ├── auxiliary/                         # Supporting features
│       │   ├── chatbot/
│       │   │   └── GptChatActivity.java       # AI chat interface
│       │   ├── profile/
│       │   │   └── EditProfileActivity.java   # Profile management
│       │   ├── security/
│       │   │   └── SecurityScanActivity.java  # Security monitoring
│       │   └── settings/
│       │       └── ServerSettingsActivity.java # Server configuration
│       ├── core/                              # Core application screens
│       │   ├── MainActivity.java              # Main dashboard
│       │   └── SplashActivity.java            # App startup
│       └── groups/                            # Group management features
│           ├── chat/
│           │   └── ChatActivity.java          # Group messaging
│           ├── creation/
│           │   └── CreateGroupActivity.java   # Group creation
│           ├── discovery/
│           │   ├── JoinGroupActivity.java     # Join groups
│           │   └── PublicGroupsActivity.java  # Browse public groups
│           ├── main/
│           │   └── PartyMainActivity.java     # Group main screen
│           ├── management/                    # Group administration
│           │   ├── AdminOptionsActivity.java  # Admin tools
│           │   ├── AdminSettingsActivity.java # Admin settings
│           │   └── ChangeDateActivity.java    # Event date management
│           └── members/                       # Member management
│               ├── FriendsAddActivity.java    # Add friends
│               ├── FriendsRemoveActivity.java # Remove friends
│               ├── MembersComingActivity.java # Attendee list
│               ├── MembersInvitedActivity.java # Invitation list
│               └── UsersListActivity.java     # User directory
├── utils/                                     # Utility Classes
│   ├── auth/                                  # Authentication utilities
│   │   ├── AuthenticationManager.java         # Auth operations
│   │   └── SecureAuthenticationManager.java   # Enhanced security
│   ├── business/                              # Business logic utilities
│   │   ├── group/
│   │   │   ├── GroupDataManager.java          # Group data operations
│   │   │   └── GroupDateTimeManager.java      # Date/time management
│   │   └── sharing/
│   │       └── ContentSharingManager.java     # Content sharing logic
│   ├── core/                                  # Core utilities
│   │   ├── AppConstants.java                  # Application constants
│   │   ├── ExtrasMetadata.java                # Intent extras metadata
│   │   └── IntentExtrasManager.java           # Intent management
│   ├── infrastructure/                        # Infrastructure utilities
│   │   ├── async/
│   │   │   └── AsyncTaskReplacement.java      # Async operations
│   │   ├── network/
│   │   │   └── NetworkErrorHandler.java       # Network error handling
│   │   ├── permissions/
│   │   │   └── PermissionManager.java         # Runtime permissions
│   │   └── system/
│   │       ├── MemoryManager.java             # Memory optimization
│   │       └── ThreadUtils.java               # Thread management
│   ├── media/                                 # Media utilities
│   │   ├── FileManager.java                   # File operations
│   │   ├── GlideImageLoader.java              # Image loading
│   │   └── ImageCompressor.java               # Image optimization
│   ├── security/                              # Security utilities
│   │   ├── core/
│   │   │   ├── PasswordValidator.java         # Password validation
│   │   │   ├── SecureConfigManager.java       # Secure configuration
│   │   │   └── SimpleSecureStorage.java       # Encrypted storage
│   │   ├── encryption/
│   │   │   ├── EncryptedSharedPreferencesManager.java
│   │   │   ├── EnhancedSecureStorage.java
│   │   │   ├── GroupKeyManager.java
│   │   │   ├── GroupMessageEncryption.java
│   │   │   ├── HybridMessageEncryption.java
│   │   │   └── MessageEncryptionManager.java
│   │   ├── monitoring/
│   │   │   ├── SecurityAgent.java             # Security monitoring
│   │   │   ├── SecurityIssue.java             # Security issue tracking
│   │   │   └── SecurityReport.java            # Security reporting
│   │   └── network/
│   │       └── SSLPinningManager.java         # SSL certificate pinning
│   ├── server/
│   │   └── ServerModeManager.java             # Server configuration
│   └── ui/                                    # UI utilities
│       ├── components/
│       │   ├── LoadingStateManager.java       # Loading state management
│       │   └── UiStateManager.java            # UI state coordination
│       ├── feedback/
│       │   ├── NotificationManager.java       # Notification handling
│       │   └── UserFeedbackManager.java       # User feedback
│       ├── maps/
│       │   └── MapUtilitiesManager.java       # Map utilities
│       └── navigation/
│           └── NavigationManager.java         # Navigation coordination
└── viewmodel/                                 # MVVM ViewModels
    ├── BaseViewModel.java                     # Base ViewModel class
    ├── auth/                                  # Authentication ViewModels
    │   ├── AuthViewModel.java                 # Authentication logic
    │   └── IntroViewModel.java                # Introduction flow
    ├── core/                                  # Core ViewModels
    │   ├── MainActivityViewModel.java         # Main screen logic
    │   └── SplashViewModel.java               # Splash screen logic
    ├── features/                              # Feature ViewModels
    │   ├── GptViewModel.java                  # AI chat logic
    │   ├── ProfileViewModel.java              # Profile management
    │   └── ServerSettingsViewModel.java       # Settings logic
    └── groups/                                # Group ViewModels
        ├── GroupChatViewModel.java            # Group chat logic
        └── GroupViewModel.java                # Group management logic
```

### Spring Boot Server Architecture

```
app/server/src/main/java/com/example/partymaker/server/
├── 🚀 PartyMakerServerApplication.java        # Spring Boot application entry
├── config/                                    # Configuration classes
│   └── FirebaseConfig.java                    # Firebase Admin SDK config
├── controller/                                # REST Controllers
│   └── FirebaseController.java                # Firebase API endpoints
└── service/                                   # Business Logic Services
    └── FirebaseService.java                   # Firebase operations service
```

### Build Configuration

```
PartyMaker/
├── 🏗️ build.gradle.kts                        # Root build configuration
├── app/
│   └── build.gradle.kts                       # Android app build config
├── app/server/
│   └── build.gradle                           # Spring Boot build config
├── gradle/
│   ├── libs.versions.toml                     # Centralized dependency versions
│   └── wrapper/                               # Gradle wrapper
├── settings.gradle.kts                        # Multi-module settings
└── gradle.properties                          # Global Gradle properties
```

## 🚀 Getting Started

### 📋 Prerequisites

#### Development Environment
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **Java Development Kit**: OpenJDK 11 or Oracle JDK 11+
- **Android SDK**: API 33+ (Android 13)
- **Gradle**: 8.9+ (via wrapper)
- **Git**: Latest version for source control

#### External Services
- **Firebase Project**: Complete Firebase project setup
- **Google Cloud Platform**: For Maps and Places APIs
- **OpenAI Account**: For GPT chatbot functionality
- **Development Device**: Android device or emulator (API 33+)

### 🛠️ Installation & Setup

#### 1. Repository Setup
```bash
# Clone the repository
git clone https://github.com/Natifishman/PartyMaker.git
cd PartyMaker

# Verify repository structure
ls -la
```

#### 2. Firebase Configuration
```bash
# Create Firebase configuration from template
cp google-services.json.template app/google-services.json

# Edit configuration with your Firebase project details
# Get from: Firebase Console > Project Settings > General
nano app/google-services.json
```

**Firebase Setup Checklist:**
- ✅ Create new Firebase project
- ✅ Enable Authentication (Email/Password, Google Sign-In)
- ✅ Enable Realtime Database with security rules
- ✅ Enable Cloud Storage with appropriate permissions
- ✅ Generate service account key for server
- ✅ Download `google-services.json` for Android

#### 3. API Keys Configuration
```bash
# Create secrets file from template
cp secrets.properties.template secrets.properties

# Add your API keys (never commit this file!)
cat >> secrets.properties << EOF
# Google Services
GOOGLE_MAPS_API_KEY=your_google_maps_api_key
PLACES_API_KEY=your_places_api_key

# OpenAI Integration
OPENAI_API_KEY=your_openai_api_key

# Firebase Server Key
FIREBASE_SERVER_KEY=your_firebase_server_key
EOF
```

#### 4. Server Configuration
```bash
# Setup Firebase Admin SDK for server
cp firebase-service-account.json.template app/server/src/main/resources/firebase-service-account.json

# Edit with your Firebase service account credentials
# Get from: Firebase Console > Project Settings > Service Accounts
nano app/server/src/main/resources/firebase-service-account.json
```

#### 5. Build & Run

##### Android Application
```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Alternative: Build without tests (faster)
./gradlew buildWithoutTests

# Format code before commits
./gradlew spotlessApply
```

##### Spring Boot Server
```bash
# Navigate to server directory
cd app/server

# Run server in development mode
./gradlew bootRun

# Build production JAR
./gradlew bootJar

# Run tests
./gradlew test
```

##### Development Workflow
```bash
# Terminal 1: Start Spring Boot server
cd app/server && ./gradlew bootRun

# Terminal 2: Run Android app
# Open Android Studio and run on device/emulator
# Or use command line:
./gradlew installDebug && adb shell am start -n com.example.partymaker/.ui.features.core.SplashActivity
```

### 🔧 Configuration Details

#### Environment Variables
| Variable Name | Description | Required | Source |
|---------------|-------------|----------|--------|
| `GOOGLE_MAPS_API_KEY` | Google Maps integration | Yes | Google Cloud Console |
| `PLACES_API_KEY` | Google Places API | Yes | Google Cloud Console |
| `OPENAI_API_KEY` | OpenAI GPT integration | Yes | OpenAI Platform |
| `FIREBASE_SERVER_KEY` | Firebase server key | Yes | Firebase Console |
| `SERVER_URL` | Custom server URL | No | Development override |

#### Firebase Security Rules Example
```javascript
// Realtime Database Rules
{
  "rules": {
    "Users": {
      "$uid": {
        ".read": "$uid === auth.uid || root.child('Groups').child($uid).child('members').child(auth.uid).exists()",
        ".write": "$uid === auth.uid"
      }
    },
    "Groups": {
      "$groupId": {
        ".read": "data.child('members').child(auth.uid).exists() || data.child('isPublic').val() === true",
        ".write": "data.child('adminKey').val() === auth.uid || !data.exists()"
      }
    },
    "GroupsMessages": {
      "$groupId": {
        ".read": "root.child('Groups').child($groupId).child('members').child(auth.uid).exists()",
        ".write": "root.child('Groups').child($groupId).child('members').child(auth.uid).exists()"
      }
    }
  }
}
```

## 📱 API Documentation

### Android Client APIs

#### Firebase Server Client
```java
// Primary server communication client
FirebaseServerClient client = FirebaseServerClient.getInstance();

// User operations
client.createUser(user, callback);
client.updateUser(userKey, updates, callback);
client.deleteUser(userKey, callback);

// Group operations
client.createGroup(group, callback);
client.getGroups(userEmail, callback);
client.updateGroup(groupKey, updates, callback);
client.deleteGroup(groupKey, callback);

// Message operations
client.sendMessage(groupKey, message, callback);
client.getMessages(groupKey, callback);
```

#### Repository Pattern Usage
```java
// Group Repository - Handles both local and remote data
GroupRepository repository = GroupRepository.getInstance();
repository.initialize(context);

// Get groups with caching
repository.getGroups(userEmail, forceRefresh, new Repository.Callback<List<Group>>() {
    @Override
    public void onSuccess(List<Group> groups) {
        // Handle successful data retrieval
    }
    
    @Override
    public void onError(Exception error) {
        // Handle error scenarios
    }
});

// Create new group
repository.createGroup(group, callback);
```

### Spring Boot Server APIs

#### Base URL
- **Production**: `https://partymaker.onrender.com`
- **Development**: `http://localhost:8080`
- **Android Emulator**: `http://10.0.2.2:8080`

#### Authentication
All API endpoints require proper Firebase authentication tokens.

```http
Authorization: Bearer <firebase_id_token>
Content-Type: application/json
```

#### Core Endpoints

##### User Management
```http
GET    /api/firebase/Users                    # Get all users
GET    /api/firebase/Users/{userKey}          # Get specific user
POST   /api/firebase/Users                    # Create new user
PUT    /api/firebase/Users/{userKey}          # Update user
DELETE /api/firebase/Users/{userKey}          # Delete user
```

##### Group Management
```http
GET    /api/firebase/Groups                   # Get all groups
GET    /api/firebase/Groups/{groupKey}        # Get specific group
POST   /api/firebase/Groups                   # Create new group
PUT    /api/firebase/Groups/{groupKey}        # Update group
DELETE /api/firebase/Groups/{groupKey}        # Delete group
```

##### Message Management
```http
GET    /api/firebase/GroupsMessages           # Get all messages
GET    /api/firebase/GroupsMessages/{groupKey}# Get group messages
POST   /api/firebase/GroupsMessages           # Send message
PUT    /api/firebase/GroupsMessages/{messageKey} # Update message
DELETE /api/firebase/GroupsMessages/{messageKey} # Delete message
```

##### Generic Data Operations
```http
GET    /api/firebase/data/{path}              # Get data from path
POST   /api/firebase/data/{path}              # Create data at path
PUT    /api/firebase/data/{path}              # Update data at path
DELETE /api/firebase/data/{path}              # Delete data at path
```

#### Request/Response Examples

##### Create Group Request
```json
POST /api/firebase/Groups
{
  "groupName": "Summer Pool Party",
  "groupLocation": "123 Main St, City, State",
  "adminKey": "user123",
  "groupDays": "15",
  "groupMonths": "July",
  "groupYears": "2024",
  "groupHours": "14:00",
  "groupMinutes": "00",
  "isPublic": true,
  "maxMembers": 50,
  "description": "Join us for an amazing summer pool party!"
}
```

##### Create Group Response
```json
{
  "success": true,
  "message": "Group created successfully",
  "data": {
    "groupKey": "group_abc123",
    "groupName": "Summer Pool Party",
    "createdAt": "2024-07-01T10:00:00Z",
    "adminKey": "user123",
    "memberCount": 1
  }
}
```

##### Error Response
```json
{
  "success": false,
  "message": "Invalid request data",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": [
      {
        "field": "groupName",
        "message": "Group name is required"
      }
    ]
  },
  "timestamp": "2024-07-01T10:00:00Z"
}
```

## 🗄️ Database Schema

### Firebase Realtime Database Structure

```json
{
  "Users": {
    "user_unique_key": {
      "username": "string",
      "email": "string", 
      "userKey": "string",
      "fullName": "string",
      "phoneNumber": "string",
      "profileImageUrl": "string",
      "createdAt": "timestamp",
      "lastActive": "timestamp",
      "friends": {
        "friend_key_1": true,
        "friend_key_2": true
      },
      "settings": {
        "notifications": "boolean",
        "privacy": "string"
      }
    }
  },
  "Groups": {
    "group_unique_key": {
      "groupName": "string",
      "groupKey": "string",
      "groupLocation": "string",
      "adminKey": "string",
      "createdAt": "timestamp",
      "groupDays": "string",
      "groupMonths": "string", 
      "groupYears": "string",
      "groupHours": "string",
      "groupMinutes": "string",
      "isPublic": "boolean",
      "maxMembers": "number",
      "description": "string",
      "groupImageUrl": "string",
      "members": {
        "member_key_1": {
          "joinedAt": "timestamp",
          "role": "admin|member",
          "status": "confirmed|pending|declined"
        }
      },
      "invitations": {
        "invitation_key_1": {
          "invitedBy": "string",
          "invitedAt": "timestamp",
          "status": "pending|accepted|declined"
        }
      },
      "settings": {
        "allowInvites": "boolean",
        "requireApproval": "boolean",
        "chatEnabled": "boolean"
      }
    }
  },
  "GroupsMessages": {
    "group_key": {
      "message_unique_key": {
        "messageText": "string",
        "senderKey": "string",
        "senderName": "string", 
        "timestamp": "timestamp",
        "messageType": "text|image|system",
        "imageUrl": "string",
        "edited": "boolean",
        "editedAt": "timestamp",
        "reactions": {
          "user_key": "emoji"
        }
      }
    }
  },
  "Notifications": {
    "user_key": {
      "notification_key": {
        "title": "string",
        "message": "string",
        "type": "group_invite|message|event_reminder",
        "timestamp": "timestamp",
        "read": "boolean",
        "actionData": "object"
      }
    }
  }
}
```

### Room Database Schema (Local Storage)

#### Users Table
```sql
CREATE TABLE users (
    userKey TEXT PRIMARY KEY NOT NULL,
    username TEXT,
    email TEXT,
    fullName TEXT,
    phoneNumber TEXT,
    profileImageUrl TEXT,
    createdAt TEXT,
    lastSynced INTEGER
);
```

#### Groups Table
```sql
CREATE TABLE groups (
    groupKey TEXT PRIMARY KEY NOT NULL,
    group_name TEXT,
    group_location TEXT,
    admin_key TEXT,
    created_at TEXT,
    group_days TEXT,
    group_months TEXT,
    group_years TEXT,
    group_hours TEXT,
    group_minutes TEXT,
    is_public INTEGER,
    max_members INTEGER,
    description TEXT,
    group_image_url TEXT,
    member_count INTEGER,
    last_synced INTEGER
);
```

#### Chat Messages Table
```sql
CREATE TABLE chat_messages (
    messageKey TEXT PRIMARY KEY NOT NULL,
    groupKey TEXT NOT NULL,
    messageText TEXT,
    senderKey TEXT,
    senderName TEXT,
    timestamp INTEGER,
    messageType TEXT,
    imageUrl TEXT,
    edited INTEGER DEFAULT 0,
    editedAt INTEGER,
    localOnly INTEGER DEFAULT 0,
    FOREIGN KEY(groupKey) REFERENCES groups(groupKey)
);
```

## 🧪 Testing Strategy

### Testing Architecture

PartyMaker implements a comprehensive testing strategy with multiple layers:

#### 1. Unit Testing
```bash
# Run Android unit tests
./gradlew test

# Run server unit tests  
cd app/server && ./gradlew test

# Generate test coverage report
./gradlew jacocoTestReport
```

#### 2. Integration Testing
```bash
# Run Android instrumented tests
./gradlew connectedAndroidTest

# Run server integration tests
cd app/server && ./gradlew integrationTest
```

#### 3. UI Testing (Espresso)
```java
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = 
        new ActivityTestRule<>(MainActivity.class);
    
    @Test
    public void testGroupListDisplay() {
        onView(withId(R.id.recyclerViewGroups))
            .check(matches(isDisplayed()));
    }
}
```

#### 4. API Testing
```java
@Test
public void testCreateGroup() {
    Group testGroup = new Group();
    testGroup.setGroupName("Test Party");
    
    FirebaseServerClient.getInstance().createGroup(testGroup, new Callback<Group>() {
        @Override
        public void onSuccess(Group result) {
            assertNotNull(result.getGroupKey());
            assertEquals("Test Party", result.getGroupName());
        }
    });
}
```

### Test Configuration

#### Test Dependencies
```kotlin
dependencies {
    // Unit Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:4.11.0'
    testImplementation 'androidx.arch.core:core-testing:2.2.0'
    
    // Android Testing
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test:rules:1.5.0'
    
    // Server Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

## 🔒 Security Implementation

### Security Architecture

PartyMaker implements enterprise-grade security across all layers:

#### 1. Authentication & Authorization
- **Firebase Authentication**: Secure user identity management
- **JWT Tokens**: Stateless authentication with automatic refresh
- **Role-based Access Control**: Admin, member, and guest permissions
- **Session Management**: Secure session handling with timeout

#### 2. Data Encryption
- **End-to-End Encryption**: Message encryption for sensitive communications
- **Local Storage Encryption**: AndroidX Security Crypto for local data
- **API Communication**: HTTPS/TLS 1.3 for all network communication
- **Database Encryption**: Firebase security rules and encryption at rest

#### 3. Input Validation & Sanitization
```java
// Server-side validation example
@RestController
@Validated
public class FirebaseController {
    
    @PostMapping("/Groups")
    public ResponseEntity<Map<String, Object>> createGroup(
            @Valid @RequestBody Group group) {
        // Validation annotations ensure data integrity
        return firebaseService.createGroup(group);
    }
}
```

#### 4. Security Monitoring
```java
// Security monitoring implementation
public class SecurityAgent {
    public void reportSecurityEvent(SecurityIssue issue) {
        SecurityReport report = new SecurityReport();
        report.setIssueType(issue.getType());
        report.setSeverity(issue.getSeverity());
        report.setTimestamp(System.currentTimeMillis());
        
        // Log and potentially alert on security events
        logSecurityEvent(report);
    }
}
```

### Security Best Practices

#### API Security
- **Input Validation**: All inputs validated and sanitized
- **Rate Limiting**: API rate limiting to prevent abuse
- **CORS Configuration**: Proper CORS settings for web security
- **Error Handling**: No sensitive information in error responses

#### Client Security
- **Certificate Pinning**: SSL certificate pinning for API calls
- **Root Detection**: Protection against rooted/jailbroken devices
- **Code Obfuscation**: Release builds use ProGuard/R8 obfuscation
- **Debug Detection**: Runtime detection of debugging attempts

## 🚀 Deployment Guide

### Android Application Deployment

#### 1. Production Build Configuration
```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

#### 2. Generate Signed APK/AAB
```bash
# Generate signed APK
./gradlew assembleRelease

# Generate Android App Bundle (recommended for Play Store)
./gradlew bundleRelease

# Verify signatures
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

#### 3. Google Play Store Deployment
1. Create Play Console account and app listing
2. Upload signed AAB file
3. Configure store listing with screenshots and descriptions
4. Set up release management and testing tracks
5. Publish to production

### Spring Boot Server Deployment

#### 1. Production JAR Build
```bash
cd app/server
./gradlew clean bootJar

# JAR file created at: build/libs/partymaker-server-0.0.1-SNAPSHOT.jar
```

#### 2. Docker Containerization
```dockerfile
# Dockerfile for server deployment
FROM openjdk:11-jre-slim

COPY build/libs/partymaker-server-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build and run Docker container
docker build -t partymaker-server .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=production partymaker-server
```

#### 3. Cloud Platform Deployment

##### Render.com (Current Production)
```yaml
# render.yaml
services:
  - type: web
    name: partymaker-server
    env: java
    buildCommand: cd app/server && ./gradlew bootJar
    startCommand: java -jar build/libs/partymaker-server-*.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: FIREBASE_CONFIG
        fromFile: firebase-service-account.json
```

##### AWS Elastic Beanstalk
```bash
# Install EB CLI
pip install awsebcli

# Initialize and deploy
eb init partymaker-server
eb create production
eb deploy
```

##### Google Cloud Platform
```bash
# Deploy to Cloud Run
gcloud run deploy partymaker-server \
  --image gcr.io/PROJECT_ID/partymaker-server \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

### Environment Configuration

#### Production Environment Variables
```bash
# Server configuration
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080

# Firebase configuration
GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-service-account.json

# Database configuration
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com

# Security configuration
JWT_SECRET=your-jwt-secret
CORS_ALLOWED_ORIGINS=https://your-domain.com

# Monitoring
LOGGING_LEVEL_ROOT=INFO
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info
```

## 📊 Performance Optimization

### Android Client Optimizations

#### 1. Memory Management
```java
// Custom memory manager for low-memory scenarios
public class MemoryManager {
    public static void performMemoryCleanup(Context context) {
        // Clear image caches
        Glide.get(context).clearMemory();
        
        // Clear temporary files
        FileManager.clearTemporaryFiles(context);
        
        // Force garbage collection
        System.gc();
    }
    
    public static String getDetailedMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return String.format("Memory: %d MB used, %d MB free", 
            usedMemory / 1024 / 1024, freeMemory / 1024 / 1024);
    }
}
```

#### 2. Image Loading Optimization
```java
// Optimized Glide configuration
public class GlideImageLoader {
    public static void loadGroupImage(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
            .load(imageUrl)
            .timeout(5000) // 5-second timeout
            .placeholder(R.drawable.default_group_image)
            .error(R.drawable.default_group_image)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView);
    }
}
```

#### 3. Background Threading
```java
// Custom thread utilities for optimal performance
public class ThreadUtils {
    private static final ExecutorService BACKGROUND_EXECUTOR = 
        Executors.newFixedThreadPool(4);
    
    public static void runOnBackground(Runnable task) {
        BACKGROUND_EXECUTOR.execute(task);
    }
    
    public static void runOnUiThread(Activity activity, Runnable task) {
        activity.runOnUiThread(task);
    }
}
```

### Server Performance Optimizations

#### 1. Connection Pooling
```yaml
# application.yml - Production configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### 2. Caching Strategy
```java
@Service
@EnableCaching
public class FirebaseService {
    
    @Cacheable(value = "groups", key = "#userEmail")
    public List<Group> getUserGroups(String userEmail) {
        // Cached for better performance
        return firebaseOperations.getUserGroups(userEmail);
    }
}
```

### Performance Metrics

#### Key Performance Indicators
- **App Startup Time**: < 2 seconds cold start
- **Screen Navigation**: < 300ms transition time
- **API Response Time**: < 1 second average
- **Image Loading**: < 2 seconds for cached images
- **Memory Usage**: < 200MB average RAM usage
- **Battery Usage**: Optimized for minimal battery drain

## 🔍 Monitoring & Analytics

### Application Monitoring

#### 1. Crash Reporting
```java
// Firebase Crashlytics integration
public class CrashReportingManager {
    public static void logCrash(Exception exception) {
        FirebaseCrashlytics.getInstance().recordException(exception);
    }
    
    public static void setUserIdentifier(String userId) {
        FirebaseCrashlytics.getInstance().setUserId(userId);
    }
}
```

#### 2. Performance Monitoring
```java
// Custom performance tracking
public class PerformanceTracker {
    public static void trackScreenLoad(String screenName, long loadTime) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("screen_name", screenName);
        eventData.put("load_time_ms", loadTime);
        
        FirebaseAnalytics.getInstance(context)
            .logEvent("screen_load_time", eventData);
    }
}
```

### Business Analytics

#### User Engagement Metrics
- **Daily Active Users (DAU)**
- **Monthly Active Users (MAU)**
- **Session Duration**
- **Screen Flow Analysis**
- **Feature Usage Statistics**

#### Event Analytics
- **Event Creation Rate**
- **Invitation Response Rate**
- **Group Participation Metrics**
- **Chat Activity Levels**
- **Location Usage Patterns**

## 🤝 Contributing Guidelines

### Development Workflow

#### 1. Code Standards
```java
// Follow Java naming conventions and documentation standards
/**
 * Manages group data operations with local and remote data sources.
 * 
 * <p>This repository provides a unified interface for group data access,
 * implementing smart caching and offline support capabilities.
 * 
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class GroupRepository {
    private static final String TAG = "GroupRepository";
    
    /**
     * Retrieves groups for the specified user with optional cache refresh.
     * 
     * @param userEmail the user's email address
     * @param forceRefresh whether to bypass cache and fetch fresh data
     * @param callback callback for handling success/error responses
     */
    public void getGroups(String userEmail, boolean forceRefresh, 
                         Repository.Callback<List<Group>> callback) {
        // Implementation
    }
}
```

#### 2. Git Workflow
```bash
# Create feature branch
git checkout -b feature/user-profile-enhancement

# Make commits with conventional commit format
git commit -m "feat: add user profile image upload functionality"
git commit -m "fix: resolve image compression memory leak"
git commit -m "docs: update API documentation for profile endpoints"

# Push and create pull request
git push origin feature/user-profile-enhancement
```

#### 3. Pull Request Template
```markdown
## 🎯 Purpose
Brief description of the changes and their purpose.

## 🔄 Changes Made
- [ ] New feature implementation
- [ ] Bug fix
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Refactoring

## 🧪 Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] Performance impact assessed

## 📸 Screenshots
Include screenshots for UI changes.

## 📝 Additional Notes
Any additional context or considerations.
```

### Code Review Checklist

#### Functionality
- [ ] Code implements requirements correctly
- [ ] Edge cases are handled appropriately
- [ ] Error handling is comprehensive
- [ ] Performance implications are considered

#### Code Quality
- [ ] Code follows established patterns and conventions
- [ ] Methods are appropriately sized and focused
- [ ] Documentation is clear and comprehensive
- [ ] No code duplication or unnecessary complexity

#### Security
- [ ] No sensitive data is logged or exposed
- [ ] Input validation is properly implemented
- [ ] Authentication and authorization are correct
- [ ] No security vulnerabilities introduced

## 📄 License & Legal

### MIT License

```
MIT License

Copyright (c) 2024 PartyMaker Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Third-Party Licenses

#### Open Source Dependencies
- **Android SDK**: Apache License 2.0
- **Spring Boot**: Apache License 2.0
- **Firebase SDK**: Apache License 2.0
- **OkHttp**: Apache License 2.0
- **Glide**: BSD License
- **Room Database**: Apache License 2.0

#### Commercial Services
- **Google Maps API**: Google APIs Terms of Service
- **OpenAI API**: OpenAI Terms of Use
- **Firebase Services**: Google Cloud Terms of Service

## 📞 Support & Community

### Getting Help

#### 🐛 Bug Reports
1. **Search existing issues** first to avoid duplicates
2. **Use the bug report template** with detailed information
3. **Include steps to reproduce** the issue
4. **Provide relevant logs** and error messages
5. **Specify environment details** (Android version, device, etc.)

#### 💡 Feature Requests
1. **Check existing feature requests** to avoid duplicates
2. **Provide detailed use cases** and business justification
3. **Include mockups or wireframes** if applicable
4. **Consider implementation complexity** and technical feasibility

#### 🤔 Questions & Discussions
- **Stack Overflow**: Tag questions with `partymaker-app`
- **GitHub Discussions**: For general questions and community discussion
- **Discord Community**: Real-time chat with other developers (coming soon)

### Community Resources

#### 📚 Documentation
- **Wiki**: Comprehensive guides and tutorials
- **API Documentation**: Complete API reference
- **Architecture Guide**: Deep dive into system design
- **Best Practices**: Development guidelines and patterns

#### 🎥 Video Resources
- **Getting Started Tutorial**: Step-by-step setup guide
- **Architecture Overview**: System design explanation
- **Feature Demos**: Showcasing key capabilities
- **Developer Interviews**: Behind-the-scenes insights

### Acknowledgments

#### 🙏 Special Thanks
- **Firebase Team**: For robust backend-as-a-service platform
- **Google Maps Team**: For comprehensive location services
- **OpenAI Team**: For powerful AI integration capabilities
- **Spring Boot Community**: For excellent web framework
- **Android Developer Community**: For continuous innovation
- **Open Source Contributors**: For invaluable third-party libraries

#### 🏆 Hall of Fame
*Contributors who have made significant impacts on the project will be recognized here.*

---

<div align="center">

## 🌟 Project Status

**Current Version**: 1.0.0  
**Status**: Active Development  
**Last Updated**: August 2025  

### 📈 Project Metrics
![GitHub stars](https://img.shields.io/github/stars/Natifishman/PartyMaker?style=social)
![GitHub forks](https://img.shields.io/github/forks/Natifishman/PartyMaker?style=social)
![GitHub issues](https://img.shields.io/github/issues/Natifishman/PartyMaker)
![GitHub pull requests](https://img.shields.io/github/issues-pr/Natifishman/PartyMaker)

---

**🎉 PartyMaker - Where Every Event Becomes a Celebration! 🎉**

*Built with ❤️ by the PartyMaker Team*

*Bringing people together through the power of technology and social connection*

</div>
