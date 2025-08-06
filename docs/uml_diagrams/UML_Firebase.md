# PartyMaker - Firebase Integration UML Diagram

## 🔥 Firebase Integration Architecture

This UML diagram shows all Firebase integration components, including Authentication, Realtime Database, Storage, and Cloud Messaging services.

---

## 🏗️ Firebase Integration Class Diagram

```mermaid
classDiagram
    %% Core Firebase Integration Classes
    class FirebaseAccessManager {
        -Context context
        
        +FirebaseAccessManager(context)
        +isServerModeEnabled() boolean
        +getGroupsRef() FirebaseServerClient
        +getUsersRef() FirebaseServerClient
        +getMessagesRef() FirebaseServerClient
    }
    
    class DBRef {
        +FirebaseAuth Auth
        +FirebaseDatabase DataBase
        +DatabaseReference refGroups
        +DatabaseReference refUsers
        +DatabaseReference refMessages
        +FirebaseStorage Storage
        +StorageReference refStorage
        +String CurrentUser
        
        +init() void
        +checkImageExists(path, listener) void
    }
    
    class ServerDBRef {
        +FirebaseAuth Auth
        +FirebaseStorage Storage
        +StorageReference refStorage
        +String CurrentUser
        -FirebaseServerClient serverClient
        
        +checkImageExists(path, listener) void
        +getServerClient() FirebaseServerClient
    }
    
    %% Callback Interfaces
    class OnImageExistsListener {
        <<interface>>
        +onImageExists(exists) void
    }
    
    class FirebaseCallback~T~ {
        <<interface>>
        +onSuccess(data) void
        +onFailure(error) void
    }
    
    class DataCallback~T~ {
        <<interface>>
        +onDataLoaded(data) void
        +onError(error) void
    }
    
    class UploadCallback {
        <<interface>>
        +onProgress(progress) void
        +onSuccess(downloadUrl) void
        +onFailure(error) void
    }
    
    %% Relationships
    FirebaseAccessManager --> FirebaseServerClient : uses
    
    DBRef --> FirebaseAuth : manages
    DBRef --> FirebaseDatabase : manages
    DBRef --> FirebaseStorage : manages
    DBRef --> OnImageExistsListener : uses
    
    ServerDBRef --> FirebaseAuth : manages
    ServerDBRef --> FirebaseStorage : manages
    ServerDBRef --> FirebaseServerClient : uses
    ServerDBRef --> OnImageExistsListener : uses
    
    FirebaseCallback --> FirebaseAccessManager : used by
    DataCallback --> FirebaseAccessManager : used by
    UploadCallback --> DBRef : used by
    UploadCallback --> ServerDBRef : used by
```

---

## 🔍 Firebase Integration Components

### **🔥 Core Firebase Services:**
- **DBRef**: Central Firebase service references and initialization
- **FirebaseAccessManager**: Mode switching between direct Firebase and server-mediated access
- **ServerDBRef**: Server-side Firebase access management
- **FirebaseInitializer**: Firebase SDK initialization and configuration

### **🔐 Authentication Integration:**
- **FirebaseAuthManager**: Firebase Authentication service wrapper
- **GoogleSignInManager**: Google Sign-In integration with Firebase Auth
- **Auth State Management**: Real-time authentication state monitoring
- **Token Management**: ID token generation and validation

### **📊 Realtime Database Management:**
- **FirebaseRealtimeManager**: Core Realtime Database operations
- **GroupDataManager**: Group-specific database operations
- **UserDataManager**: User-specific database operations
- **MessageDataManager**: Message-specific database operations

### **📁 Storage Management:**
- **FirebaseStorageManager**: Core Cloud Storage operations
- **ProfileImageManager**: User profile image management
- **GroupImageManager**: Group image management
- **File Upload/Download**: Comprehensive file management with progress tracking

### **📱 Cloud Messaging:**
- **FirebaseMessagingManager**: FCM token and topic management
- **PushNotificationService**: Notification handling and display
- **Notification Categories**: Group, chat, and system notifications

---

## 🔄 Firebase Service Integration

### **📊 Data Flow Patterns:**
- **Real-time Listeners**: Live data updates via ValueEventListener and ChildEventListener
- **Single Value Events**: One-time data retrieval for specific operations
- **Batch Operations**: Efficient bulk data updates using updateChildren()
- **Transactions**: Atomic operations for critical data modifications

### **🔐 Authentication Flow:**
- **Multi-provider Auth**: Email/password and Google Sign-In support
- **Auth State Persistence**: Automatic session restoration across app restarts
- **Token Refresh**: Automatic ID token refresh for server authentication
- **Profile Management**: User profile updates and photo management

### **📁 Storage Operations:**
- **Progressive Upload**: Chunked upload with progress tracking
- **Image Optimization**: Automatic compression before upload
- **Metadata Management**: File metadata storage and retrieval
- **URL Generation**: Secure download URL generation with expiration

---

## 🎯 Advanced Firebase Features

### **⚡ Performance Optimization:**
- **Offline Persistence**: Local caching with automatic synchronization
- **Connection Management**: Smart online/offline state handling
- **Batch Operations**: Reduced network calls through batching
- **Lazy Loading**: On-demand data loading for better performance

### **🔍 Real-time Features:**
- **Live Updates**: Instant UI updates via Firebase listeners
- **Presence System**: User online/offline status tracking
- **Collaborative Editing**: Real-time group data synchronization
- **Conflict Resolution**: Automatic conflict resolution for concurrent updates

### **📱 Push Notifications:**
---

## 📋 **Firebase Summary**

### **🔥 Core Firebase Components (3)**
- **FirebaseAccessManager**: Access manager that routes to server client
- **DBRef**: Firebase references helper for Auth, Database, and Storage
- **ServerDBRef**: Server-mode replacement for direct Firebase access

### **🔧 Callback Interfaces (4)**
- **OnImageExistsListener**: Image existence check callbacks
- **FirebaseCallback<T>**: Generic success/failure callback pattern
- **DataCallback<T>**: Real-time data change event handling
- **UploadCallback**: File upload progress and completion tracking

### **🏗️ Architecture**
- **Server-First Approach**: Uses Spring Boot server instead of direct Firebase access
- **Firebase Services**: Auth and Storage still used directly for specific features
- **Simple Integration**: Lightweight wrapper classes around Firebase SDK
- **Callback Management**: Clean callback interfaces for async operations

---

*Simplified Firebase integration with 3 core classes and 4 callback interfaces, using server-first architecture for data operations.* 