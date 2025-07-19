# PartyMaker 🎉

![Project Views](https://komarev.com/ghpvc/?username=Natifishman&repo=PartyMaker&color=blue&style=flat-square)

A comprehensive social event planning platform with a modern Android client and Spring Boot server architecture. PartyMaker enables users to create, manage, and participate in social events with real-time communication, group management, and AI-powered features.

## 🏗️ Architecture Overview

PartyMaker follows a **client-server architecture** with clear separation of concerns:

- **Android Client**: Native Android application built with Kotlin/Java
- **Spring Boot Server**: RESTful API server handling business logic and data operations
- **Firebase Backend**: Authentication, real-time database, and cloud services
- **AI Integration**: OpenAI-powered chatbot for enhanced user experience

## 🛠️ Technology Stack

### 📱 Android Client
- **Language**: Kotlin & Java
- **Minimum SDK**: API 33 (Android 13)
- **Target SDK**: API 35 (Android 15)
- **UI Framework**: Traditional Android Views with Material Design
- **Architecture**: MVVM with Repository Pattern and ViewModels
- **Build System**: Gradle with Version Catalogs

### 🖥️ Spring Boot Server
- **Framework**: Spring Boot 2.7.5
- **Language**: Java 11
- **Architecture**: RESTful API with Service Layer
- **Database**: Firebase Realtime Database (via Admin SDK)

### 🔥 Firebase Services
- **Authentication**: User registration, login, and session management
- **Realtime Database**: Real-time data synchronization
- **Cloud Storage**: File and image storage

### 🎨 UI & Design
- **Material Design**: Modern UI components and theming
- **Custom Animations**: Smooth transitions and loading effects
- **Responsive Layout**: Adaptive design for different screen sizes
- **Consistent Toolbars**: Professional UI across all screens

### 🤖 AI & External Services
- **OpenAI Integration**: Intelligent chatbot using GPT models
- **Google Maps**: Location services and mapping
- **Google Places**: Location search and autocomplete

## ✨ Key Features

### 🔐 User Management
- **Secure Authentication**: Firebase Auth with email/password and Google Sign-In
- **Profile Management**: Complete user profile creation and editing
- **Friend System**: Add, remove, and manage friends
- **User Discovery**: Browse and connect with other users

### 👥 Group & Community
- **Group Creation**: Public and private group management
- **Membership Control**: Join, leave, and admin group operations
- **Group Administration**: Advanced admin tools and settings
- **Group Activities**: Dedicated group interaction screens

### 🎊 Event Planning
- **Event Creation**: Comprehensive event setup with date/time management
- **Invitation System**: Send and track event invitations
- **Attendance Tracking**: Monitor event participation
- **Event Administration**: Tools for event organizers

### 💬 Real-time Communication
- **In-app Messaging**: Real-time chat within groups
- **Message History**: Persistent conversation storage
- **AI Chatbot**: Intelligent assistant for user support
- **Local Notifications**: App-level notification system for party updates

### 🗺️ Location Services
- **Google Maps Integration**: Interactive maps for event locations
- **Location Search**: Google Places integration for venue discovery
- **Geolocation**: User location tracking and sharing

### 🔄 Social Sharing
- **Party Sharing**: Share party details via multiple channels
- **WhatsApp Integration**: Direct sharing to WhatsApp
- **Facebook Integration**: Share parties on Facebook
- **SMS & Email Sharing**: Send party invites via SMS or email
- **Text-based Sharing**: Generate shareable party descriptions

### 🔔 Notifications
- **Real-time Notifications**: Get notified about party updates
- **Customizable Channels**: Separate channels for parties, messages, and updates
- **Subscription Management**: Subscribe/unsubscribe to specific party notifications
- **Visual Indicators**: Clear notification icons and descriptions

## 📁 Project Structure

```
PartyMaker/
├── app/                          # Android Application
│   ├── src/main/
│   │   ├── java/com/example/partymaker/
│   │   │   ├── data/             # Data layer (API, Firebase, Models)
│   │   │   │   ├── api/          # Network communication
│   │   │   │   ├── firebase/     # Firebase integration
│   │   │   │   ├── model/        # Data models
│   │   │   │   └── repository/   # Data repositories
│   │   │   ├── ui/               # UI components and activities
│   │   │   │   ├── adapters/     # RecyclerView and ListView adapters
│   │   │   │   ├── auth/         # Authentication screens
│   │   │   │   ├── group/        # Group management screens
│   │   │   │   ├── chatbot/      # AI chatbot interface
│   │   │   │   ├── profile/      # User profile screens
│   │   │   │   └── common/       # Shared UI components
│   │   │   ├── utilities/        # Helper classes and utilities
│   │   │   │   ├── NotificationHelper.java  # Notification management
│   │   │   │   ├── ShareHelper.java         # Social sharing features
│   │   │   │   └── groupBuilder/            # Group creation utilities
│   │   │   └── viewmodel/        # ViewModels for MVVM architecture
│   │   └── res/                  # Android resources
│   └── build.gradle.kts          # Android build configuration
├── app/server/                   # Spring Boot Server
│   ├── src/main/java/
│   │   └── com/example/partymaker/server/
│   │       ├── controller/       # REST API controllers
│   │       ├── service/          # Business logic services
│   │       └── config/           # Configuration classes
│   └── build.gradle             # Server build configuration
├── gradle/
│   └── libs.versions.toml       # Centralized dependency management
└── build.gradle.kts             # Root build configuration
```

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Latest stable version (Hedgehog or newer)
- **Java Development Kit**: JDK 11 or higher
- **Android SDK**: API level 33+ (Android 13)
- **Firebase Account**: For backend services
- **Google Cloud Account**: For Maps and Places APIs
- **OpenAI API Key**: For chatbot functionality

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Natifishman/PartyMaker.git
   cd PartyMaker
   ```

2. **Firebase Configuration**
   ```bash
   # Copy Firebase configuration
   cp google-services.json.template app/google-services.json
   
   # Edit with your Firebase project configuration
   # Get configuration from Firebase Console > Project Settings
   ```

3. **API Keys Setup**
   ```bash
   # Create secrets.properties file
   cp secrets.properties.template app/secrets.properties
   
   # Add your API keys:
   # - GOOGLE_MAPS_API_KEY=your_maps_api_key
   # - OPENAI_API_KEY=your_openai_api_key
   # - FIREBASE_SERVER_KEY=your_firebase_server_key
   ```

4. **Server Configuration**
   ```bash
   # Copy Firebase service account
   cp firebase-service-account.json.template app/server/src/main/resources/firebase-service-account.json
   
   # Edit with your Firebase Admin SDK service account
   ```

5. **Build and Run**
   ```bash
   # Build Android app
   ./gradlew assembleDebug
   
   # Run Spring Boot server
   cd app/server
   ./gradlew bootRun
   
   # Run Android app
   # Open in Android Studio and run on device/emulator
   ```

6. **Running Server Locally (Emergency Fallback)**
   ```bash
   # If cloud server is down, run locally
   cd server
   ./mvnw spring-boot:run
   
   # Then update server URL in app settings:
   # - Emulator: http://10.0.2.2:8080
   # - Physical device: http://[your-computer-ip]:8080
   ```

## 🔧 Configuration

### Required Environment Variables

| Variable              | Description         | Source               |
|-----------------------|---------------------|----------------------|
| `GOOGLE_MAPS_API_KEY` | Google Maps API key | Google Cloud Console |
| `OPENAI_API_KEY`      | OpenAI API key      | OpenAI Platform      |
| `FIREBASE_SERVER_KEY` | Firebase Server key | Firebase Console     |

### Firebase Setup

1. Create a new Firebase project
2. Enable Authentication (Email/Password, Google Sign-In)
3. Enable Realtime Database
4. Enable Cloud Storage
5. Download `google-services.json` and `firebase-service-account.json`

### Google Cloud Setup

1. Enable Google Maps API
2. Enable Places API
3. Create API key with appropriate restrictions

## 📱 API Endpoints

### Server API (Spring Boot)

| Endpoint                       | Method | Description                 |
|--------------------------------|--------|-----------------------------|
| `/api/firebase/data/{path}`    | GET    | Retrieve data from Firebase |
| `/api/firebase/data/{path}`    | POST   | Save data to Firebase       |
| `/api/firebase/data/{path}`    | PUT    | Update data in Firebase     |
| `/api/firebase/data/{path}`    | DELETE | Delete data from Firebase   |
| `/api/firebase/Users`          | GET    | Get all users               |
| `/api/firebase/Groups`         | GET    | Get all groups              |
| `/api/firebase/GroupsMessages` | GET    | Get all messages            |

### Client API (Android)

- **Firebase Server Client**: Direct Firebase operations
- **OpenAI API Client**: Chatbot integration
- **Google Maps API**: Location services

## 🧪 Testing

### Android Testing
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### Server Testing
```bash
# Run server tests
cd app/server
./gradlew test
```

## 📦 Build Variants

### Android
- **Debug**: Development build with debugging enabled
- **Release**: Production build with optimizations

### Server
- **Development**: Local development with hot reload
- **Production**: Optimized for deployment

## 🔒 Security Considerations

- **API Key Management**: All sensitive keys stored in `secrets.properties`
- **Firebase Security Rules**: Configured for secure data access
- **Authentication**: Firebase Auth with secure token management
- **HTTPS**: All API communications use HTTPS
- **Memory Management**: Optimized to prevent memory leaks

## 🐞 Bug Fixes & Improvements

- **Back Navigation**: Fixed navigation flow between activities
- **UI Consistency**: Standardized toolbars and backgrounds across all screens
- **Memory Leaks**: Fixed memory leaks in AsyncTask operations
- **Friend Management**: Improved add/remove friend functionality
- **Profile Display**: Fixed profile image loading and user data display
- **Group Creation**: Resolved group creation and management issues
- **Error Handling**: Enhanced error handling and user feedback

## 🚀 Deployment

### Android App
1. Generate signed APK/AAB
2. Upload to Google Play Console
3. Configure Firebase for production

### Spring Boot Server
1. Build JAR file: `./gradlew bootJar`
2. Deploy to cloud platform (AWS, GCP, Azure)
3. Configure environment variables
4. Set up SSL certificates

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Follow coding standards**:
   - Use consistent naming conventions
   - Add JavaDoc comments for public methods
   - Follow MVVM architecture patterns
4. **Test your changes**: Ensure all tests pass
5. **Commit with clear messages**: Use conventional commit format
6. **Create a Pull Request**: Provide detailed description

### Development Guidelines

- **Code Style**: Follow Android and Java coding conventions
- **Architecture**: Maintain MVVM patterns and separation of concerns
- **Testing**: Write unit tests for new features
- **Documentation**: Update README and add inline comments
- **Security**: Never commit API keys or sensitive data

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: Report bugs via [GitHub Issues](https://github.com/Natifishman/PartyMaker/issues)
- **Documentation**: Check the [Wiki](https://github.com/Natifishman/PartyMaker/wiki)
- **Discussions**: Join community discussions

## 🙏 Acknowledgments

- **Firebase**: For robust backend services
- **Google Maps**: For location services
- **OpenAI**: For AI-powered chatbot
- **Spring Boot**: For server framework
- **Material Design**: For modern UI components

---

**Built with ❤️ for bringing people together through amazing events!**

*PartyMaker - Where every event becomes a celebration*
