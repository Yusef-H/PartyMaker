# Development Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Development Environment](#development-environment)
3. [Project Structure](#project-structure)
4. [Coding Standards](#coding-standards)
5. [Development Workflow](#development-workflow)
6. [Testing](#testing)
7. [Debugging](#debugging)
8. [Contributing](#contributing)

## Getting Started

### Quick Start
```bash
# Clone the repository
git clone https://github.com/yourusername/partymaker.git
cd partymaker

# Setup configuration files
cp secrets.properties.template secrets.properties
cp google-services.json.template app/google-services.json
# Edit these files with your API keys and configuration

# Build the project
./gradlew clean build

# Run the backend server
cd app/server
./gradlew bootRun

# Install Android app on connected device
cd ../..
./gradlew installDebug
```

### Prerequisites

#### Software Requirements
- **Java Development Kit (JDK)**: Version 11 or higher
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **Android SDK**: API Level 33-35
- **Gradle**: 7.6 or higher (included via wrapper)
- **Git**: Version control
- **Node.js**: 14+ (for Firebase tools)

#### Hardware Requirements
- **RAM**: Minimum 8GB (16GB recommended)
- **Storage**: 10GB free space
- **Processor**: Intel i5 or equivalent

## Development Environment

### 1. Android Studio Setup

#### Install Android Studio
1. Download from [developer.android.com](https://developer.android.com/studio)
2. Install with Android SDK
3. Configure SDK Manager:
   - Android SDK Platform 35
   - Android SDK Build-Tools 34.0.0
   - Google Play services
   - Android Emulator

#### Import Project
```bash
# Open Android Studio
# File → Open → Select project root directory
# Wait for Gradle sync to complete
```

#### Configure IDE
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND" value="99" />
      <option name="NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND" value="99" />
    </JavaCodeStyleSettings>
    <JetCodeStyleSettings>
      <option name="CODE_STYLE_DEFAULTS" value="KOTLIN_OFFICIAL" />
    </JetCodeStyleSettings>
  </code_scheme>
</component>
```

### 2. Firebase Setup

#### Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project "partymaker-dev"
3. Enable Authentication, Realtime Database, Storage
4. Download `google-services.json` to `app/` directory

#### Configure Firebase Rules
```json
// Realtime Database Rules
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "groups": {
      "$groupId": {
        ".read": "auth != null && (data.child('members').hasChild(auth.uid) || !data.child('isPrivate').val())",
        ".write": "auth != null && data.child('adminId').val() === auth.uid"
      }
    }
  }
}
```

### 3. Backend Development

#### Local Server Setup
```bash
# Navigate to server directory
cd app/server

# Install dependencies
./gradlew build

# Configure application.properties
cp src/main/resources/application.properties.template src/main/resources/application.properties
# Edit with your Firebase configuration

# Run locally
./gradlew bootRun

# Server available at http://localhost:8080
```

#### Environment Variables
```bash
# .env file for local development
FIREBASE_PROJECT_ID=partymaker-dev
FIREBASE_DATABASE_URL=https://partymaker-dev.firebaseio.com
FIREBASE_STORAGE_BUCKET=partymaker-dev.appspot.com
GOOGLE_APPLICATION_CREDENTIALS=./firebase-service-account.json
SERVER_PORT=8080
```

### 4. API Keys Configuration

Create `secrets.properties`:
```properties
# Google Maps API Key
MAPS_API_KEY=AIzaSy...your_key_here

# OpenAI API Key
OPENAI_API_KEY=sk-...your_key_here

# Firebase Web API Key
FIREBASE_WEB_API_KEY=AIzaSy...your_key_here

# Server URLs
SERVER_URL_PROD=https://partymaker.onrender.com
SERVER_URL_DEV=http://10.0.2.2:8080
SERVER_URL_LOCAL=http://localhost:8080
```

## Project Structure

### Android Application Structure
```
app/src/main/java/com/example/partymaker/
├── PartyApplication.java          # Application class
├── data/                          # Data layer
│   ├── api/                      # Network clients
│   │   ├── FirebaseServerClient.java
│   │   ├── NetworkManager.java
│   │   └── OpenAiApi.java
│   ├── firebase/                  # Firebase integration
│   │   ├── DBRef.java
│   │   └── FirebaseAccessManager.java
│   ├── local/                     # Local database
│   │   ├── AppDatabase.java
│   │   ├── GroupDao.java
│   │   └── UserDao.java
│   ├── model/                     # Data models
│   │   ├── Group.java
│   │   ├── User.java
│   │   └── ChatMessage.java
│   └── repository/                # Repository pattern
│       ├── GroupRepository.java
│       └── UserRepository.java
├── ui/                            # Presentation layer
│   ├── adapters/                  # RecyclerView adapters
│   ├── base/                      # Base classes
│   └── features/                  # Feature modules
│       ├── auth/                  # Authentication
│       ├── core/                  # Main screens
│       └── groups/                # Group features
├── utils/                         # Utility classes
│   ├── auth/                      # Authentication utils
│   ├── business/                  # Business logic
│   ├── infrastructure/            # System utilities
│   ├── media/                     # Media handling
│   ├── security/                  # Security features
│   └── ui/                        # UI utilities
└── viewmodel/                     # ViewModels
    ├── auth/                      # Auth ViewModels
    ├── core/                      # Core ViewModels
    └── groups/                    # Group ViewModels
```

### Key Architectural Components

#### Repository Pattern
```java
public class GroupRepository {
    private final RemoteGroupDataSource remoteDataSource;
    private final LocalGroupDataSource localDataSource;
    private final MediatorLiveData<Result<List<Group>>> groups;
    
    public LiveData<Result<List<Group>>> getGroups(String userId) {
        return new NetworkBoundResource<List<Group>, List<Group>>() {
            @Override
            protected void saveCallResult(List<Group> item) {
                localDataSource.saveGroups(item);
            }
            
            @Override
            protected boolean shouldFetch(List<Group> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch();
            }
            
            @Override
            protected LiveData<List<Group>> loadFromDb() {
                return localDataSource.getGroups();
            }
            
            @Override
            protected LiveData<ApiResponse<List<Group>>> createCall() {
                return remoteDataSource.fetchGroups(userId);
            }
        }.asLiveData();
    }
}
```

#### ViewModel Implementation
```java
public class MainActivityViewModel extends BaseViewModel {
    private final GroupRepository groupRepository;
    private final MutableLiveData<List<Group>> _groups = new MutableLiveData<>();
    public final LiveData<List<Group>> groups = _groups;
    
    public void loadGroups() {
        disposables.add(
            groupRepository.getGroups(getCurrentUserId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        if (result.isSuccess()) {
                            _groups.setValue(result.getData());
                        } else {
                            handleError(result.getError());
                        }
                    },
                    this::handleError
                )
        );
    }
}
```

## Coding Standards

### Java Code Style

#### Naming Conventions
```java
// Classes: PascalCase
public class GroupRepository { }

// Methods: camelCase
public void loadUserGroups() { }

// Constants: UPPER_SNAKE_CASE
public static final int MAX_GROUP_SIZE = 50;

// Variables: camelCase
private String userName;

// Packages: lowercase
package com.example.partymaker.data.repository;
```

#### Code Organization
```java
public class ExampleActivity extends BaseActivity {
    // 1. Constants
    private static final String TAG = "ExampleActivity";
    private static final int REQUEST_CODE = 100;
    
    // 2. Static fields
    private static volatile Instance instance;
    
    // 3. Instance fields
    private GroupViewModel viewModel;
    private ActivityExampleBinding binding;
    
    // 4. Lifecycle methods (in order)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        setupListeners();
        loadData();
    }
    
    // 5. Override methods
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Implementation
    }
    
    // 6. Public methods
    public void refreshData() {
        // Implementation
    }
    
    // 7. Private methods
    private void initializeViews() {
        // Implementation
    }
    
    // 8. Inner classes/interfaces
    private class DataObserver implements Observer<List<Group>> {
        // Implementation
    }
}
```

### XML Naming Conventions

#### Layout Files
```
activity_*.xml       // Activities
fragment_*.xml       // Fragments
item_*.xml          // RecyclerView items
dialog_*.xml        // Dialogs
view_*.xml          // Custom views
layout_*.xml        // Include layouts
```

#### Resource IDs
```xml
<!-- Views -->
<TextView android:id="@+id/tv_user_name" />
<EditText android:id="@+id/et_email" />
<Button android:id="@+id/btn_submit" />
<ImageView android:id="@+id/iv_profile" />
<RecyclerView android:id="@+id/rv_groups" />

<!-- Containers -->
<LinearLayout android:id="@+id/ll_container" />
<FrameLayout android:id="@+id/fl_content" />
<ConstraintLayout android:id="@+id/cl_root" />
```

### Documentation Standards

#### Class Documentation
```java
/**
 * Repository for managing group data operations.
 * Implements Repository pattern with local caching and remote synchronization.
 * 
 * @author Team PartyMaker
 * @since 1.0.0
 */
public class GroupRepository {
    
    /**
     * Fetches groups for specified user with automatic caching.
     * 
     * @param userId The ID of the user whose groups to fetch
     * @return LiveData containing Result wrapper with group list
     * @throws IllegalArgumentException if userId is null or empty
     */
    public LiveData<Result<List<Group>>> getGroups(String userId) {
        // Implementation
    }
}
```

## Development Workflow

### 1. Feature Development

#### Branch Strategy
```bash
# Create feature branch
git checkout -b feature/group-chat-improvements

# Make changes and commit
git add .
git commit -m "feat: add message encryption to group chat"

# Push to remote
git push origin feature/group-chat-improvements

# Create pull request for review
```

#### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Testing
- `chore`: Maintenance

Example:
```
feat(chat): add end-to-end encryption

- Implement AES-256 encryption for messages
- Add key exchange protocol
- Update UI to show encryption status

Closes #123
```

### 2. Code Review Process

#### Pull Request Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No warnings introduced
```

### 3. Build Variants

#### Configure Build Types
```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "API_URL", "\"http://10.0.2.2:8080\"")
        }
        
        staging {
            initWith(debug)
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            buildConfigField("String", "API_URL", "\"https://staging.partymaker.com\"")
        }
        
        release {
            minifyEnabled = true
            shrinkResources = true
            proguardFiles(getDefaultProguardFile('proguard-android-optimize.txt'))
            buildConfigField("String", "API_URL", "\"https://api.partymaker.com\"")
        }
    }
}
```

## Testing

### 1. Unit Testing

#### Test Structure
```java
@RunWith(MockitoJUnitRunner.class)
public class GroupRepositoryTest {
    
    @Mock
    private RemoteGroupDataSource remoteDataSource;
    
    @Mock
    private LocalGroupDataSource localDataSource;
    
    private GroupRepository repository;
    
    @Before
    public void setup() {
        repository = new GroupRepository(remoteDataSource, localDataSource);
    }
    
    @Test
    public void getGroups_whenCacheEmpty_fetchesFromRemote() {
        // Given
        when(localDataSource.getGroups()).thenReturn(new MutableLiveData<>(null));
        when(remoteDataSource.fetchGroups(anyString())).thenReturn(mockGroups());
        
        // When
        LiveData<Result<List<Group>>> result = repository.getGroups("user123");
        
        // Then
        verify(remoteDataSource).fetchGroups("user123");
        assertNotNull(result.getValue());
    }
}
```

### 2. Integration Testing

#### Instrumented Tests
```java
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);
    
    @Test
    public void clickCreateGroup_opensCreateGroupActivity() {
        // Click FAB
        onView(withId(R.id.fab_create_group))
            .perform(click());
        
        // Verify CreateGroupActivity is displayed
        onView(withId(R.id.et_group_name))
            .check(matches(isDisplayed()));
    }
}
```

### 3. Testing Commands

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests="*.GroupRepositoryTest"

# Generate test coverage report
./gradlew jacocoTestReport

# Run backend tests
cd app/server
./gradlew test
```

## Debugging

### 1. Android Studio Debugger

#### Setting Breakpoints
1. Click line number gutter to set breakpoint
2. Run app in debug mode (Shift+F9)
3. Use debugger panel to inspect variables
4. Step through code with F8 (step over) or F7 (step into)

#### Conditional Breakpoints
```java
// Right-click breakpoint → More → Enter condition
userId.equals("test_user")
```

### 2. Logging

#### Logcat Usage
```java
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starting activity");
        
        try {
            loadData();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Failed to load data", e);
        }
    }
}
```

#### Filtering Logs
```bash
# Filter by tag
adb logcat -s MainActivity

# Filter by priority
adb logcat *:E  # Errors only

# Save to file
adb logcat -d > logs.txt
```

### 3. Network Debugging

#### Charles Proxy Setup
1. Install Charles Proxy
2. Configure Android emulator:
```bash
# Set proxy
emulator -avd Pixel_4_API_30 -http-proxy http://localhost:8888
```

3. Install Charles certificate on device
4. Monitor network traffic

#### Stetho Integration
```java
// Add to Application class
public class PartyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
```

### 4. Memory Profiling

#### Using Memory Profiler
1. Run app with profiler attached
2. View → Tool Windows → Profiler
3. Select Memory timeline
4. Force garbage collection
5. Capture heap dump
6. Analyze object allocations

#### LeakCanary Integration
```kotlin
dependencies {
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}
```

## Contributing

### 1. Getting Started

#### Fork and Clone
```bash
# Fork repository on GitHub
# Clone your fork
git clone https://github.com/yourusername/partymaker.git
cd partymaker

# Add upstream remote
git remote add upstream https://github.com/original/partymaker.git
```

#### Stay Updated
```bash
# Fetch upstream changes
git fetch upstream

# Merge upstream changes
git checkout main
git merge upstream/main
```

### 2. Development Process

1. **Pick an Issue**: Choose from [GitHub Issues](https://github.com/partymaker/issues)
2. **Create Branch**: `git checkout -b feature/issue-number-description`
3. **Make Changes**: Follow coding standards
4. **Test**: Run all tests
5. **Commit**: Use conventional commits
6. **Push**: `git push origin feature/your-branch`
7. **Pull Request**: Create PR with description

### 3. Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive criticism
- Follow project guidelines
- Report unacceptable behavior

### 4. Review Process

#### Review Criteria
- Code quality and standards
- Test coverage
- Documentation
- Performance impact
- Security considerations

#### Merge Requirements
- Two approvals required
- All tests passing
- No merge conflicts
- Documentation updated

## Troubleshooting

### Common Issues

#### Gradle Build Failures
```bash
# Clean and rebuild
./gradlew clean
./gradlew --stop
rm -rf ~/.gradle/caches/
./gradlew build --refresh-dependencies
```

#### Firebase Connection Issues
```bash
# Verify configuration
cat app/google-services.json | jq '.project_info.project_id'

# Test connection
curl https://your-project.firebaseio.com/.json
```

#### Emulator Issues
```bash
# Cold boot emulator
emulator -avd Pixel_4_API_30 -no-snapshot-load

# Wipe emulator data
emulator -avd Pixel_4_API_30 -wipe-data
```

## Resources

### Documentation
- [Android Developers](https://developer.android.com)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Spring Boot Guides](https://spring.io/guides)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [Postman](https://www.postman.com)
- [Charles Proxy](https://www.charlesproxy.com)
- [Stetho](http://facebook.github.io/stetho)

### Learning Resources
- [Codelabs](https://codelabs.developers.google.com)
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [Material Design](https://material.io/design)

---

*Development Guide Version: 1.0.0 | Last Updated: August 2025*