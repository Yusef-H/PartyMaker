# PartyMaker - Advanced User Flow Diagrams
## Enterprise-Level Flow Diagrams - Based on Deep Architecture Analysis

---

## 🎯 Authentication & User Session Flow

```mermaid
graph TD
    A[SplashActivity<br/>Application Initialization] --> B{Authentication State Check}
    B -->|No Active Session| C[IntroActivity<br/>Introduction Screen]
    B -->|Active Session Not Authenticated| D[LoginActivity<br/>Login Screen]
    B -->|Authenticated Session| E[MainActivity<br/>Main Screen]
    
    C --> F[3 Introduction Screens<br/>Onboarding Sliders]
    F --> D
    
    D --> G{Authentication Type}
    G -->|Email + Password| H[AuthenticationManager<br/>Firebase Email Auth]
    G -->|Google Sign-In| I[SecureAuthenticationManager<br/>Google OAuth2]
    G -->|Reset Password| J[ResetPasswordActivity]
    G -->|New Registration| K[RegisterActivity]
    
    H --> L[PasswordValidator<br/>Security Check]
    L --> M{Validation Check}
    M -->|Valid| N[Firebase Authentication]
    M -->|Invalid| O[Specific Error Message]
    
    I --> P[Google Sign-In API]
    P --> Q[Get Google Token]
    Q --> N
    
    N --> R[AuthenticationManager.setCurrentUser]
    R --> S[EncryptedSharedPreferencesManager<br/>Encrypted Session Storage]
    S --> T[PartyMakerPrefs<br/>user_explicitly_logged_in = true]
    T --> E
    
    K --> U[Registration with Real-time Validation]
    U --> V[PasswordValidator + SecureConfigManager]
    V --> W{All Fields Valid?}
    W -->|Yes| N
    W -->|No| X[Specific Error Messages]
    
    J --> Y[Email Input]
    Y --> Z[Send Reset Link]
    Z --> AA[Firebase Password Reset]
    
    O --> D
    X --> K
    
    E --> BB[MainActivity.forceSetServerUrl]
    BB --> CC[NetworkManager.initialize]
    CC --> DD[Load User Groups]
    
    style A fill:#e3f2fd
    style E fill:#c8e6c9
    style N fill:#e8f5e8
    style S fill:#fff8e1
```

---

## 🏠 MainActivity Architecture & Data Flow

```mermaid
graph TD
    A[MainActivity] --> B[PartyApplication.onCreate]
    B --> C[System Components Initialization]
    C --> D[Firebase.initializeApp]
    C --> E[NetworkManager.initialize]
    C --> F[GroupRepository.initialize]
    C --> G[ConnectivityManager.getInstance]
    
    A --> H[MainActivityViewModel]
    H --> I[GroupRepository]
    I --> J{Repository Pattern}
    J --> K[LocalGroupDataSource<br/>Room Database]
    J --> L[RemoteGroupDataSource<br/>FirebaseServerClient]
    
    L --> M[NetworkUtils.executeWithRetry]
    M --> N{Connection Check}
    N -->|Connected| O[HTTP Request<br/>https://partymaker.onrender.com]
    N -->|Not Connected| P[Use Local Cache]
    
    O --> Q[FirebaseController.getUserGroups]
    Q --> R[FirebaseService.getData]
    R --> S[Firebase Realtime Database]
    
    A --> T[MainActivity.observeViewModel]
    T --> U[LiveData.observe]
    U --> V{Update Type}
    V -->|New Groups| W[GroupAdapter.updateItems]
    V -->|Loading State| X[LoadingStateManager.showLoading]
    V -->|Error| Y[UiStateManager.showError]
    
    W --> Z[RecyclerView UI Update]
    X --> AA[ProgressBar Displayed]
    Y --> BB[Error Snackbar]
    
    A --> CC{User Actions}
    CC -->|Pull-to-Refresh| DD[SwipeRefreshLayout]
    CC -->|Group Click| EE[navigateToGroupScreen]
    CC -->|AI Button| FF[navigateToChat]
    CC -->|Menu| GG[onOptionsItemSelected]
    
    DD --> HH[viewModel.loadUserGroups<br/>forceRefresh=true]
    EE --> II[PartyMainActivity<br/>ExtrasMetadata]
    FF --> JJ[GptChatActivity]
    GG --> KK[ServerSettingsActivity<br/>or Logout]
    
    A --> LL{Memory Management}
    LL --> MM[MemoryManager.performMemoryCleanup]
    LL --> NN[ThreadUtils.runOnMainThread]
    LL --> OO[onResume: REFRESH_COOLDOWN_MS Check]
    
    style A fill:#e1f5fe
    style H fill:#e8f5e8
    style I fill:#fff3e0
    style O fill:#f3e5f5
    style S fill:#ffebee
```

---

## 🎉 PartyMainActivity - Comprehensive Group Management Flow

```mermaid
graph TD
    A[PartyMainActivity] --> B[Intent.getStringExtra<br/>GroupKey + UserKey]
    B --> C[PartyMainViewModel.initialize]
    C --> D[8 Feature Cards Layout]
    
    D --> E[Card1: Group Information<br/>Group Details]
    D --> F[Card2: Attendance Status<br/>Attendance Status]
    D --> G[Card3: Invited Members<br/>MembersInvitedActivity]
    D --> H[Card4: Confirmed Members<br/>MembersComingActivity]
    D --> I[Card5: Admin Operations<br/>AdminOptionsActivity]
    D --> J[Card6: Group Chat<br/>ChatActivity]
    D --> K[Card7: Invite Friends<br/>FriendsAddActivity]
    D --> L[Card8: Leave Group<br/>Group Exit]
    
    E --> M[Group Details Display]
    M --> N[Name, Description, Date<br/>Location, Price, Type]
    M --> O[GroupViewModel.observeGroup]
    
    F --> P{Attendance Selection}
    P -->|Coming| Q[comingKeys userKey = true]
    P -->|Not Coming| R[comingKeys userKey = false]
    P -->|Unknown| S[comingKeys userKey = null]
    
    Q --> T[GroupRepository.updateGroup]
    R --> T
    S --> T
    T --> U[Firebase Update<br/>Real-time Sync]
    
    G --> V[MembersInvitedAdapter]
    V --> W[friendKeys HashMap Display]
    W --> X[User Status Indicators]
    
    H --> Y[MembersComingAdapter]
    Y --> Z[Filtered Coming Members]
    Z --> AA[Statistics & Count]
    
    I --> BB{Permission Check}
    BB -->|adminKey equals userKey| CC[Admin Panel Access]
    BB -->|Regular Member| DD[Access Denied Dialog]
    
    CC --> EE[AdminSettingsActivity]
    CC --> FF[ChangeDateActivity<br/>Date & Time Management]
    CC --> GG[Group Deletion<br/>with Confirmation]
    CC --> HH[Member Management<br/>Add/Remove Users]
    
    J --> II[ChatActivity<br/>Real-time Messaging]
    II --> JJ[GroupChatViewModel]
    JJ --> KK[MessageEncryptionManager<br/>E2E Encryption]
    KK --> LL[GroupKeyManager<br/>Key Distribution]
    LL --> MM[Firebase GroupsMessages]
    
    K --> NN[FriendsAddActivity]
    NN --> OO{Add Method}
    OO -->|Manual Email| PP[Email Input + Validation]
    OO -->|Contact List| QQ[Device Contacts Access]
    
    PP --> RR[UserRepository.findByEmail]
    QQ --> SS[Permission Request<br/>READ_CONTACTS]
    RR --> TT[Add to friendKeys]
    SS --> UU[Contact Selection]
    UU --> TT
    
    L --> VV{Confirmation Dialog}
    VV -->|Confirm| WW[GroupRepository.leaveGroup]
    VV -->|Cancel| A
    
    WW --> XX[Remove from friendKeys]
    XX --> YY[GroupKeyManager.removeUserAndRotateKey]
    YY --> ZZ[Navigation Back to MainActivity]
    
    A --> AAA[Security Layer Integration]
    AAA --> BBB[SSLPinningManager<br/>Secure Communications]
    AAA --> CCC[SecurityAgent<br/>Threat Monitoring]
    AAA --> DDD[PasswordValidator<br/>Input Validation]
    
    A --> EEE[Performance Optimization]
    EEE --> FFF[ThreadUtils<br/>Background Operations]
    EEE --> GGG[MemoryManager<br/>Resource Cleanup]
    EEE --> HHH[LoadingStateManager<br/>UI State Control]
    
    style A fill:#fff3e0
    style I fill:#fce4ec
    style J fill:#f3e5f5
    style AAA fill:#fff8e1
    style EEE fill:#f1f8e9
    style MM fill:#ffebee
```

---

## 🤖 Advanced AI Assistant Integration - OpenAI ChatGPT Flow

```mermaid
graph TD
    A[GptChatActivity] --> B[GptViewModel.initialize]
    B --> C[ChatbotAdapter Setup]
    C --> D[RecyclerView with Chat Messages]
    
    A --> E{User Input Processing}
    E -->|Text Input| F[Message Validation]
    E -->|Voice Input| G[Speech-to-Text Recognition]
    E -->|Image Input| H[Image Processing & OCR]
    
    F --> I[GptViewModel.sendMessage]
    G --> I
    H --> I
    
    I --> J[OpenAiApi.createChatCompletion]
    J --> K[Build Request Headers]
    K --> L[API Key from secrets.properties]
    L --> M[SSL Pinned HTTPS Request]
    
    M --> N[OpenAI GPT-4 API]
    N --> O{API Response}
    O -->|Success 200| P[Parse JSON Response]
    O -->|Rate Limited 429| Q[Exponential Backoff Retry]
    O -->|Error 4xx/5xx| R[Error Handling & User Notification]
    
    P --> S[ChatMessageGpt.fromResponse]
    S --> T[Conversation Context Management]
    T --> U[ChatbotAdapter.addMessage]
    U --> V[RecyclerView Animation]
    
    Q --> W[ThreadUtils.runOnMainThreadDelayed]
    W --> X[Retry Counter Check]
    X -->|< Max Retries| J
    X -->|>= Max Retries| R
    
    R --> Y[UiStateManager.showError]
    Y --> Z[Fallback Response Generation]
    
    A --> AA[Message Persistence Layer]
    AA --> BB[Room Database ChatMessageDao]
    BB --> CC[Local Message History]
    CC --> DD[Offline Message Queue]
    
    A --> EE{Advanced Features}
    EE -->|Context Awareness| FF[Group-Specific Suggestions]
    EE -->|Party Planning| GG[Event Management Tips]
    EE -->|Location Services| HH[MapUtilitiesManager Integration]
    EE -->|Calendar Integration| II[Date/Time Suggestions]
    
    FF --> JJ[Group Details Analysis]
    GG --> KK[Budget & Planning Algorithms]
    HH --> LL[Nearby Venues API]
    II --> MM[Google Calendar API]
    
    A --> NN[Security & Privacy]
    NN --> OO[Message Encryption at Rest]
    NN --> PP[API Key Protection]
    NN --> QQ[User Data Anonymization]
    NN --> RR[NetworkErrorHandler.sanitizeResponse]
    
    A --> SS{Performance Optimization}
    SS --> TT[Message Chunking for Long Responses]
    SS --> UU[Background Processing]
    SS --> VV[Memory Management]
    SS --> WW[Network Request Caching]
    
    style A fill:#f3e5f5
    style N fill:#fff3e0
    style P fill:#e8f5e8
    style R fill:#ffebee
    style NN fill:#fff8e1
```

---

## 👤 Advanced Profile Management & Security Integration

```mermaid
graph TD
    A[EditProfileActivity] --> B[ProfileViewModel.initialize]
    B --> C[User Data Loading]
    C --> D[AuthenticationManager.getCurrentUser]
    
    A --> E{Profile Operations}
    E -->|Profile Picture| F[Image Selection & Processing]
    E -->|Username Edit| G[Username Validation & Update]
    E -->|Password Change| H[Secure Password Update]
    E -->|Account Settings| I[Privacy & Security Settings]
    
    F --> J[FileManager.selectFromGallery]
    J --> K[PermissionManager.checkStoragePermission]
    K --> L{Permission Granted?}
    L -->|Yes| M[ImageCompressor.compressForUpload]
    L -->|No| N[Permission Request Dialog]
    
    M --> O[GlideImageLoader.loadPreview]
    O --> P[Firebase Storage Upload]
    P --> Q[UserRepository.updateProfilePicture]
    
    G --> R[ProfileViewModel.validateUsername]
    R --> S[PasswordValidator.validateInput]
    S --> T{Validation Results}
    T -->|Valid| U[UserRepository.updateUser]
    T -->|Invalid| V[Error Display with Specific Messages]
    
    H --> W[SecureAuthenticationManager.changePassword]
    W --> X[Current Password Verification]
    X --> Y[PasswordValidator.validateNewPassword]
    Y --> Z[EnhancedSecureStorage.updateCredentials]
    
    I --> AA[Privacy Settings Panel]
    AA --> BB[EncryptedSharedPreferencesManager]
    BB --> CC[Security Preferences Update]
    
    A --> DD[Network Layer Integration]
    DD --> EE[ConnectivityManager.isNetworkAvailable]
    EE --> FF{Connection Status}
    FF -->|Online| GG[FirebaseServerClient.updateUser]
    FF -->|Offline| HH[LocalUserDataSource.queueUpdate]
    
    GG --> II[NetworkUtils.executeWithRetry]
    II --> JJ{Server Response}
    JJ -->|Success| KK[UI Success Feedback]
    JJ -->|Failure| LL[Error Handling & Retry Logic]
    
    HH --> MM[Pending Operations Queue]
    MM --> NN[Network State Observer]
    NN -->|Connection Restored| OO[Sync Pending Changes]
    
    A --> PP[Security Monitoring]
    PP --> QQ[SecurityAgent.logProfileAccess]
    PP --> RR[SimpleSecureStorage.auditTrail]
    PP --> SS[Threat Detection & Response]
    
    A --> TT[UI State Management]
    TT --> UU[LoadingStateManager.showProgress]
    TT --> VV[UiStateManager.handleErrors]
    TT --> WW[UserFeedbackManager.showSuccess]
    
    A --> XX{Advanced Features}
    XX -->|Biometric Auth| YY[BiometricAuthManager]
    XX -->|Two-Factor Auth| ZZ[2FA Setup & Verification]
    XX -->|Account Recovery| AAA[Secure Account Recovery]
    XX -->|Data Export| BBB[GDPR Compliance Export]
    
    style A fill:#e8f5e8
    style PP fill:#fff8e1
    style XX fill:#f3e5f5
    style KK fill:#c8e6c9
    style LL fill:#ffebee
```

---

## ⚙️ Complete System Architecture - Enterprise-Level Infrastructure

```mermaid
graph TD
    A[PartyMaker Android Client] --> B[PartyApplication.onCreate]
    B --> C[System Initialization Layer]
    
    C --> D[Firebase.initializeApp]
    C --> E[NetworkManager.initialize]
    C --> F[MemoryManager.initialize]
    C --> G[Repository Initialization]
    
    G --> H[GroupRepository.getInstance]
    G --> I[UserRepository.getInstance]
    
    H --> J{Data Source Strategy}
    J --> K[LocalGroupDataSource<br/>Room AppDatabase]
    J --> L[RemoteGroupDataSource<br/>FirebaseServerClient]
    
    I --> M[LocalUserDataSource<br/>Room UserDao]
    I --> N[RemoteUserDataSource<br/>Firebase Users API]
    
    L --> O[NetworkUtils.executeWithRetry]
    O --> P{Network Layer}
    P -->|Primary| Q[Spring Boot Server<br/>partymaker.onrender.com:443]
    P -->|Fallback| R[Local Dev Server<br/>localhost:8080]
    P -->|Direct| S[Firebase Direct Access]
    
    Q --> T[FirebaseController.java]
    T --> U[FirebaseService.java]
    U --> V[Firebase Admin SDK]
    V --> W[Firebase Realtime Database]
    
    A --> X{MVVM Architecture}
    X --> Y[Presentation Layer]
    X --> Z[Business Logic Layer]
    X --> AA[Data Layer]
    
    Y --> BB[Activities & Fragments]
    BB --> CC[Authentication Flow<br/>Splash→Intro→Login→Register]
    BB --> DD[Core App Flow<br/>MainActivity→PartyMain→Features]
    BB --> EE[Auxiliary Features<br/>Profile→Settings→Security→AI]
    
    Z --> FF[ViewModels]
    FF --> GG[MainActivityViewModel]
    FF --> HH[AuthViewModel]
    FF --> II[GroupViewModel]
    FF --> JJ[ProfileViewModel]
    FF --> KK[GptViewModel]
    
    AA --> LL[Repository Pattern Implementation]
    LL --> MM[Data Abstraction Layer]
    MM --> NN[Local Storage<br/>Room + SharedPreferences]
    MM --> OO[Remote Storage<br/>Firebase + Custom Server]
    
    A --> PP{Security Infrastructure}
    PP --> QQ[Authentication Layer]
    QQ --> RR[AuthenticationManager<br/>Firebase Auth]
    QQ --> SS[SecureAuthenticationManager<br/>Enhanced Security]
    
    PP --> TT[Encryption Layer]
    TT --> UU[GroupKeyManager<br/>E2E Message Encryption]
    TT --> VV[EncryptedSharedPreferencesManager<br/>Local Data Encryption]
    TT --> WW[MessageEncryptionManager<br/>Chat Security]
    
    PP --> XX[Network Security]
    XX --> YY[SSLPinningManager<br/>Certificate Pinning]
    XX --> ZZ[NetworkErrorHandler<br/>Secure Error Handling]
    
    PP --> AAA[Security Monitoring]
    AAA --> BBB[SecurityAgent<br/>Threat Detection]
    AAA --> CCC[PasswordValidator<br/>Input Validation]
    
    A --> DDD{Performance & Infrastructure}
    DDD --> EEE[Memory Management]
    EEE --> FFF[MemoryManager<br/>Low Memory Cleanup]
    EEE --> GGG[LoadingStateManager<br/>UI State Control]
    
    DDD --> HHH[Threading & Async]
    HHH --> III[ThreadUtils<br/>Background Operations]
    HHH --> JJJ[AsyncTaskReplacement<br/>Modern Async Pattern]
    
    DDD --> KKK[Network Optimization]
    KKK --> LLL[ConnectivityManager<br/>Network State Monitoring]
    KKK --> MMM[Retry Mechanisms<br/>Exponential Backoff]
    
    A --> NNN{External Integrations}
    NNN --> OOO[Google Services]
    OOO --> PPP[Google Sign-In API]
    OOO --> QQQ[Google Maps API]
    
    NNN --> RRR[OpenAI Integration]
    RRR --> SSS[OpenAiApi<br/>GPT-4 Chat Completion]
    RRR --> TTT[Chat Context Management]
    
    NNN --> UUU[Device Integration]
    UUU --> VVV[PermissionManager<br/>Runtime Permissions]
    UUU --> WWW[FileManager<br/>Media Access]
    UUU --> XXX[NotificationManager<br/>Push Notifications]
    
    A --> YYY{Data Flow Patterns}
    YYY --> ZZZ[Cache-First Strategy]
    ZZZ --> AAAA[Local Cache Check]
    ZZZ --> BBBB[Remote Fetch on Miss]
    ZZZ --> CCCC[Background Sync]
    
    YYY --> DDDD[Offline Support]
    DDDD --> EEEE[Pending Operations Queue]
    DDDD --> FFFF[Conflict Resolution]
    DDDD --> GGGG[Network State Recovery]
    
    style A fill:#e1f5fe
    style PP fill:#fff8e1
    style DDD fill:#f1f8e9
    style NNN fill:#f3e5f5
    style YYY fill:#e8f5e8
```

---

## 🔄 Advanced Data Synchronization & Conflict Resolution

```mermaid
graph TD
    A[User Operation Trigger] --> B[GroupRepository.getUserGroups]
    B --> C{Cache-First Strategy}
    C -->|Cache Hit| D[LocalGroupDataSource.getAllItems]
    C -->|Cache Miss| E[RemoteGroupDataSource.getUserGroups]
    C -->|Force Refresh| E
    
    D --> F[Data Age Check]
    F --> G{Data Freshness}
    G -->|Fresh| H[Return Cached Data]
    G -->|Stale| I[Background Refresh Trigger]
    
    I --> J[NetworkUtils.executeWithRetry]
    J --> K{Network Status}
    K -->|Available| L[FirebaseServerClient.getUserGroups]
    K -->|Unavailable| M[Fallback to Cache + User Notification]
    
    L --> N[HTTP Request with Retry Logic]
    N --> O{Server Response}
    O -->|200 Success| P[Parse & Transform Data]
    O -->|4xx Client Error| Q[Error Handling & User Feedback]
    O -->|5xx Server Error| R[Exponential Backoff Retry]
    O -->|Network Timeout| S[Retry with Increased Timeout]
    
    P --> T[Data Validation & Sanitization]
    T --> U[LocalGroupDataSource.saveItem Cache Update]
    U --> V[LiveData.setValue UI Update]
    
    E --> W[Direct Server Communication]
    W --> X[MainActivityViewModel.handleSuccessfulGroupsLoad]
    X --> Y[Sort & Filter User Groups]
    Y --> Z[GroupAdapter.updateItems]
    
    A --> AA{Write Operations}
    AA -->|Create Group| BB[GroupRepository.saveGroup]
    AA -->|Update Group| CC[GroupRepository.updateGroup]
    AA -->|Delete Group| DD[GroupRepository.deleteGroup]
    AA -->|Join Group| EE[GroupRepository.joinGroup]
    AA -->|Leave Group| FF[GroupRepository.leaveGroup]
    
    BB --> GG[Optimistic UI Update]
    CC --> GG
    DD --> GG
    EE --> GG
    FF --> GG
    
    GG --> HH[Queue for Remote Sync]
    HH --> II{Network Status}
    II -->|Online| JJ[Immediate Server Sync]
    II -->|Offline| KK[Pending Operations Queue]
    
    JJ --> LL{Sync Result}
    LL -->|Success| MM[Remove from Queue]
    LL -->|Failure| NN[Revert Optimistic Update]
    LL -->|Conflict| OO[Conflict Resolution Strategy]
    
    KK --> PP[ConnectivityManager.NetworkStateObserver]
    PP --> QQ{Connection Restored}
    QQ -->|Yes| RR[Process Pending Queue]
    QQ -->|No| SS[Continue Monitoring]
    
    RR --> TT[FIFO Queue Processing]
    TT --> UU[Batch Sync Operations]
    UU --> VV{Batch Result}
    VV -->|All Success| WW[Clear Queue]
    VV -->|Partial Success| XX[Retry Failed Operations]
    VV -->|All Failed| YY[Exponential Backoff]
    
    OO --> ZZ{Conflict Type}
    ZZ -->|Timestamp Based| AAA[Last Write Wins]
    ZZ -->|User Priority| BBB[Admin Override]
    ZZ -->|Data Merge| CCC[Intelligent Merge Algorithm]
    ZZ -->|Manual Resolution| DDD[User Conflict Dialog]
    
    A --> EEE[Security Layer Integration]
    EEE --> FFF[GroupKeyManager.addUserToGroupEncryption]
    EEE --> GGG[MessageEncryptionManager Sync]
    EEE --> HHH[SecurityAgent.auditDataAccess]
    
    A --> III[Performance Optimization]
    III --> JJJ[ThreadUtils.runOnBackgroundThread]
    III --> KKK[MemoryManager.optimizeForOperation]
    III --> LLL[Pagination for Large Datasets]
    III --> MMM[Incremental Sync Delta Updates]
    
    style A fill:#e1f5fe
    style H fill:#c8e6c9
    style V fill:#c8e6c9
    style OO fill:#fff3e0
    style NN fill:#ffebee
    style EEE fill:#fff8e1
    style III fill:#f1f8e9
```

---

## 📊 Advanced Performance & Resource Management

```mermaid
graph TD
    A[PartyApplication.onCreate] --> B[System Performance Initialization]
    B --> C[MemoryManager.initialize]
    C --> D[ThreadUtils.setupExecutors]
    D --> E[LoadingStateManager.initialize]
    
    A --> F[Memory Management Layer]
    F --> G[ComponentCallbacks2.onTrimMemory]
    G --> H{Memory Pressure Level}
    H -->|TRIM_MEMORY_CRITICAL| I[Emergency Memory Cleanup]
    H -->|TRIM_MEMORY_MODERATE| J[Proactive Memory Management]
    H -->|TRIM_MEMORY_BACKGROUND| K[Background Cleanup]
    
    I --> L[Glide.clearMemory]
    I --> M[Room.clearAllTables]
    I --> N[ViewModel.clearAllData]
    I --> O[System.gc Force]
    
    J --> P[Image Cache Reduction]
    J --> Q[Network Cache Cleanup]
    J --> R[Unused Object Cleanup]
    
    K --> S[Background Data Purge]
    K --> T[Old File Cleanup]
    
    A --> U[Threading Architecture]
    U --> V{Operation Type Classification}
    V -->|UI Updates| W[ThreadUtils.runOnMainThread]
    V -->|Network I/O| X[Background ThreadPoolExecutor]
    V -->|Database Operations| Y[Single Thread IO Executor]
    V -->|CPU Intensive| Z[Parallel Processing Pool]
    V -->|File Operations| AA[File I/O Thread]
    
    X --> BB[FirebaseServerClient Operations]
    BB --> CC[SSL Pinned Connections]
    CC --> DD[Connection Pooling & Reuse]
    DD --> EE[Request Timeout Management]
    
    Y --> FF[Room Database Operations]
    FF --> GG[Transaction Batching]
    GG --> HH[SQL Query Optimization]
    HH --> II[Index Usage Analysis]
    
    Z --> JJ[Image Processing Pipeline]
    JJ --> KK[ImageCompressor.compressForUpload]
    KK --> LL[Progressive JPEG Encoding]
    LL --> MM[Multi-threaded Processing]
    
    A --> NN[Activity Lifecycle Optimization]
    NN --> OO{Lifecycle State Management}
    OO -->|onCreate| PP[Minimal Resource Allocation]
    OO -->|onStart| QQ[Service Binding]
    OO -->|onResume| RR[Data Refresh with Cooldown]
    OO -->|onPause| SS[State Persistence]
    OO -->|onStop| TT[Resource Release]
    OO -->|onDestroy| UU[Complete Cleanup]
    
    RR --> VV[REFRESH_COOLDOWN_MS Check]
    VV --> WW{Cooldown Elapsed?}
    WW -->|Yes| XX[Force Refresh Data]
    WW -->|No| YY[Skip Refresh]
    
    SS --> ZZ[EncryptedSharedPreferencesManager.save]
    TT --> AAA[Network Connection Cleanup]
    UU --> BBB[Observer Unregistration]
    BBB --> CCC[Memory Leak Prevention]
    
    A --> DDD[Network Performance Layer]
    DDD --> EEE[ConnectivityManager.NetworkStateTracking]
    EEE --> FFF[Bandwidth Detection]
    FFF --> GGG[Adaptive Quality Settings]
    
    DDD --> HHH[NetworkUtils.executeWithRetry]
    HHH --> III[Exponential Backoff Strategy]
    III --> JJJ[Circuit Breaker Pattern]
    JJJ --> KKK[Request Deduplication]
    
    A --> LLL[UI Performance Optimization]
    LLL --> MMM[RecyclerView ViewHolder Recycling]
    MMM --> NNN[DiffUtil for Efficient Updates]
    NNN --> OOO[View Binding Optimization]
    
    LLL --> PPP[LoadingStateManager.showLoading]
    PPP --> QQQ[Progressive UI Rendering]
    QQQ --> RRR[Skeleton Screen Implementation]
    
    LLL --> SSS[UiStateManager Error Handling]
    SSS --> TTT[User-Friendly Error Messages]
    TTT --> UUU[Graceful Degradation]
    
    A --> VVV[Monitoring & Analytics]
    VVV --> WWW[Performance Metrics Collection]
    WWW --> XXX[Memory Usage Tracking]
    XXX --> YYY[Network Latency Monitoring]
    YYY --> ZZZ[User Experience Analytics]
    
    style A fill:#e1f5fe
    style I fill:#ffebee
    style U fill:#f1f8e9
    style DDD fill:#fff3e0
    style LLL fill:#f3e5f5
    style VVV fill:#e8f5e8
```

---

*These diagrams provide a comprehensive view of all flows in the PartyMaker application at Enterprise professional level*