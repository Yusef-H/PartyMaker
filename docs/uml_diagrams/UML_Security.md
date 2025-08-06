# PartyMaker - Security & Authentication UML Diagram

## 🔐 Security Architecture & Authentication Components

This UML diagram shows all security-related classes, authentication mechanisms, and encryption components in the PartyMaker application.

---

## 🏗️ Security & Authentication Class Diagram

```mermaid
classDiagram
    %% Core Security Classes
    class SecurityAgent {
        -Context context
        -EncryptedSharedPreferencesManager prefsManager
        -SSLPinningManager sslManager
        -Map~String,SecurityEvent~ securityLog
        -boolean isMonitoringEnabled
        
        +SecurityAgent(context)
        +logGroupAccess(groupKey, userKey) void
        +validateGroupPermissions(groupKey, userKey) boolean
        +auditAdminAction(action, groupKey, userKey) void
        +checkGroupIntegrity(group) boolean
        +performSecurityScan() SecurityScanResult
        +detectSuspiciousActivity(userKey) boolean
        +generateSecurityReport() SecurityReport
        +enableSecurityMonitoring() void
        +disableSecurityMonitoring() void
        +clearSecurityLog() void
        -validateUserPermissions(userKey, action) boolean
        -logSecurityEvent(event) void
        -analyzeSecurityPatterns() void
    }
    
    class PasswordValidator {
        -int minLength
        -boolean requireUppercase
        -boolean requireLowercase
        -boolean requireNumbers
        -boolean requireSpecialChars
        -List~String~ commonPasswords
        
        +PasswordValidator()
        +validatePassword(password) ValidationResult
        +validateEmail(email) ValidationResult
        +validateUsername(username) ValidationResult
        +checkPasswordStrength(password) PasswordStrength
        +validateConfirmPassword(password, confirm) ValidationResult
        +isPasswordCompromised(password) boolean
        +generateSecurePassword(length) String
        -hasUppercase(password) boolean
        -hasLowercase(password) boolean
        -hasNumbers(password) boolean
        -hasSpecialChars(password) boolean
        -isCommonPassword(password) boolean
        -calculatePasswordEntropy(password) double
    }
    
    class ValidationResult {
        -boolean valid
        -String errorMessage
        -List~String~ errors
        -Map~String,String~ fieldErrors
        -ValidationLevel level
        
        +ValidationResult()
        +ValidationResult(valid, errorMessage)
        +isValid() boolean
        +setValid(valid) void
        +getErrorMessage() String
        +setErrorMessage(errorMessage) void
        +getErrors() List~String~
        +addError(error) void
        +getFieldErrors() Map~String,String~
        +addFieldError(field, error) void
        +hasErrors() boolean
        +clearErrors() void
        +getValidationLevel() ValidationLevel
        +setValidationLevel(level) void
    }
    
    %% Authentication Components
    class AuthenticationManager {
        -FirebaseAuth firebaseAuth
        -Context context
        -EncryptedSharedPreferencesManager prefsManager
        -SecurityAgent securityAgent
        -MutableLiveData~User~ currentUser
        -AuthenticationState authState
        
        +AuthenticationManager(context)
        +signInWithEmail(email, password) Task~AuthResult~
        +createUserWithEmail(email, password) Task~AuthResult~
        +signInWithGoogle(credential) Task~AuthResult~
        +signOut() void
        +getCurrentUser() FirebaseUser
        +isUserLoggedIn() boolean
        +setCurrentUser(user) void
        +saveUserSession(user) void
        +clearUserSession() void
        +sendPasswordResetEmail(email) Task~Void~
        +refreshAuthToken() Task~String~
        +validateAuthState() boolean
        -updateUserPreferences(user) void
        -logAuthenticationEvent(event, success) void
    }
    
    class SecureAuthenticationManager {
        -GoogleSignInClient googleSignInClient
        -AuthenticationManager authManager
        -Context context
        -SecurityAgent securityAgent
        
        +SecureAuthenticationManager(context)
        +initializeGoogleSignIn() void
        +performGoogleSignIn() Intent
        +handleGoogleSignInResult(result) void
        +revokeGoogleAccess() void
        +disconnectGoogleAccount() void
        +validateGoogleCredentials(credential) boolean
        +isGoogleSignInAvailable() boolean
        -configureGoogleSignIn() GoogleSignInOptions
        -logGoogleAuthEvent(event, success) void
    }
    
    %% Encryption & Key Management
    class GroupKeyManager {
        -SecretKey groupKey
        -Cipher cipher
        -Map~String,SecretKey~ userKeys
        -KeyGenerator keyGenerator
        -SecureRandom secureRandom
        
        +GroupKeyManager(groupId)
        +addUserToGroupEncryption(userKey) void
        +removeUserAndRotateKey(userKey) void
        +encryptGroupMessage(message) String
        +decryptGroupMessage(encryptedMessage) String
        +rotateGroupKey() void
        +exportUserKey(userKey) String
        +importUserKey(userKey, keyData) void
        +validateKeyIntegrity() boolean
        -generateGroupKey() SecretKey
        -deriveUserKey(userKey, groupKey) SecretKey
        -encryptKey(key, password) String
        -decryptKey(encryptedKey, password) SecretKey
    }
    
    class MessageEncryptionManager {
        -Cipher aesCipher
        -KeyGenerator keyGenerator
        -SecureRandom secureRandom
        
        +MessageEncryptionManager()
        +encryptMessage(message, key) String
        +decryptMessage(encryptedMessage, key) String
        +generateMessageKey() SecretKey
        +encryptWithPassword(message, password) String
        +decryptWithPassword(encryptedMessage, password) String
        +hashMessage(message) String
        +verifyMessageIntegrity(message, hash) boolean
        -initializeCipher(mode, key) void
        -generateIV() byte[]
        -combineIVAndCipherText(iv, cipherText) String
        -separateIVAndCipherText(combined) Pair~byte[],byte[]~
    }
    
    class EncryptedSharedPreferencesManager {
        -SharedPreferences encryptedPrefs
        -MasterKey masterKey
        -Context context
        
        +EncryptedSharedPreferencesManager(context)
        +saveString(key, value) void
        +getString(key, defaultValue) String
        +saveBoolean(key, value) void
        +getBoolean(key, defaultValue) boolean
        +saveInt(key, value) void
        +getInt(key, defaultValue) int
        +saveLong(key, value) void
        +getLong(key, defaultValue) long
        +saveUserSession(user) void
        +getUserSession() User
        +clearUserSession() void
        +clearAll() void
        +contains(key) boolean
        -createMasterKey() MasterKey
        -createEncryptedPreferences() SharedPreferences
    }
    
    %% Network Security
    class SSLPinningManager {
        -Set~String~ pinnedCertificates
        -OkHttpClient secureClient
        -Context context
        
        +SSLPinningManager(context)
        +createSecureClient() OkHttpClient
        +addCertificatePin(hostname, pin) void
        +removeCertificatePin(hostname) void
        +validateCertificate(certificate) boolean
        +isConnectionSecure(url) boolean
        +getSecureSocketFactory() SSLSocketFactory
        +getTrustManager() X509TrustManager
        -loadPinnedCertificates() void
        -createCertificatePinner() CertificatePinner
        
        <<TrustManager>>
        class CustomTrustManager {
            +checkClientTrusted(chain, authType) void
            +checkServerTrusted(chain, authType) void
            +getAcceptedIssuers() X509Certificate[]
        }
    }
    
    class SecureConfigManager {
        -Properties secureConfig
        -String configPath
        
        +SecureConfigManager()
        +getSecureProperty(key) String
        +setSecureProperty(key, value) void
        +loadSecureConfig() void
        +saveSecureConfig() void
        +isConfigSecure() boolean
        +validateConfiguration() boolean
        -encryptConfigValue(value) String
        -decryptConfigValue(encryptedValue) String
    }
    
    %% Security Monitoring & Auditing
    class SecurityEvent {
        -String eventId
        -String eventType
        -String userKey
        -String resourceKey
        -long timestamp
        -String ipAddress
        -String userAgent
        -Map~String,Object~ metadata
        -SecurityLevel level
        
        +SecurityEvent(type, userKey)
        +getEventId() String
        +getEventType() String
        +getUserKey() String
        +getResourceKey() String
        +setResourceKey(resourceKey) void
        +getTimestamp() long
        +getIpAddress() String
        +setIpAddress(ipAddress) void
        +getMetadata() Map~String,Object~
        +addMetadata(key, value) void
        +getSecurityLevel() SecurityLevel
        +setSecurityLevel(level) void
        +toJson() String
        +fromJson(json) SecurityEvent
    }
    
    class SecurityScanResult {
        -List~SecurityIssue~ issues
        -SecurityLevel overallLevel
        -long scanTimestamp
        -String scanId
        -Map~String,Object~ scanMetrics
        
        +SecurityScanResult()
        +addIssue(issue) void
        +getIssues() List~SecurityIssue~
        +getIssuesByLevel(level) List~SecurityIssue~
        +getOverallLevel() SecurityLevel
        +calculateOverallLevel() void
        +getScanTimestamp() long
        +getScanId() String
        +getScanMetrics() Map~String,Object~
        +addMetric(key, value) void
        +hasHighSeverityIssues() boolean
        +generateReport() SecurityReport
    }
    
    class SecurityIssue {
        -String issueId
        -String title
        -String description
        -SecurityLevel severity
        -String category
        -String recommendation
        -boolean resolved
        
        +SecurityIssue(title, description, severity)
        +getIssueId() String
        +getTitle() String
        +getDescription() String
        +getSeverity() SecurityLevel
        +getCategory() String
        +setCategory(category) void
        +getRecommendation() String
        +setRecommendation(recommendation) void
        +isResolved() boolean
        +markResolved() void
        +markUnresolved() void
    }
    
    %% Enums & Value Objects
    class SecurityLevel {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        CRITICAL
    }
    
    class AuthenticationState {
        <<enumeration>>
        UNAUTHENTICATED
        AUTHENTICATING
        AUTHENTICATED
        EXPIRED
        INVALID
    }
    
    class PasswordStrength {
        <<enumeration>>
        VERY_WEAK
        WEAK
        MEDIUM
        STRONG
        VERY_STRONG
    }
    
    class ValidationLevel {
        <<enumeration>>
        INFO
        WARNING
        ERROR
        CRITICAL
    }

    %% Relationships
    SecurityAgent --> EncryptedSharedPreferencesManager : uses
    SecurityAgent --> SSLPinningManager : uses
    SecurityAgent --> SecurityEvent : creates
    SecurityAgent --> SecurityScanResult : generates
    
    AuthenticationManager --> EncryptedSharedPreferencesManager : uses
    AuthenticationManager --> SecurityAgent : uses
    AuthenticationManager --> FirebaseAuth : uses
    
    SecureAuthenticationManager --> AuthenticationManager : uses
    SecureAuthenticationManager --> GoogleSignInClient : uses
    SecureAuthenticationManager --> SecurityAgent : uses
    
    PasswordValidator --> ValidationResult : returns
    PasswordValidator --> PasswordStrength : evaluates
    
    GroupKeyManager --> MessageEncryptionManager : uses
    GroupKeyManager --> SecretKey : manages
    
    MessageEncryptionManager --> Cipher : uses
    MessageEncryptionManager --> KeyGenerator : uses
    
    EncryptedSharedPreferencesManager --> MasterKey : uses
    EncryptedSharedPreferencesManager --> SharedPreferences : wraps
    
    SSLPinningManager --> CustomTrustManager : contains
    SSLPinningManager --> OkHttpClient : configures
    
    SecurityEvent --> SecurityLevel : uses
    SecurityScanResult --> SecurityIssue : contains
    SecurityScanResult --> SecurityLevel : uses
    SecurityIssue --> SecurityLevel : uses
    
    ValidationResult --> ValidationLevel : uses
    AuthenticationManager --> AuthenticationState : manages
```

---

## 🔍 Security Architecture Components

### **🛡️ Core Security Management:**
- **SecurityAgent**: Central security monitoring, auditing, and threat detection
- **PasswordValidator**: Comprehensive password strength validation and policy enforcement
- **ValidationResult**: Structured validation feedback with multiple severity levels

### **🔐 Authentication Systems:**
- **AuthenticationManager**: Firebase authentication with session management
- **SecureAuthenticationManager**: Google Sign-In with enhanced security validation
- **Multi-factor Support**: Extensible architecture for additional authentication methods

### **🔑 Encryption & Key Management:**
- **GroupKeyManager**: End-to-end encryption for group communications
- **MessageEncryptionManager**: Message-level encryption with key rotation
- **EncryptedSharedPreferencesManager**: Secure local storage with hardware-backed encryption

### **🌐 Network Security:**
- **SSLPinningManager**: Certificate pinning for secure HTTPS connections
- **SecureConfigManager**: Encrypted configuration management
- **Custom Trust Management**: Enhanced certificate validation

---

## 🔒 Security Features Implementation

### **🔐 Authentication Security:**
- **Multi-provider Support**: Firebase Auth and Google Sign-In integration
- **Session Management**: Secure token storage and automatic refresh
- **State Validation**: Real-time authentication state monitoring
- **Audit Logging**: Comprehensive authentication event tracking

### **🛡️ Data Protection:**
- **End-to-End Encryption**: Group messages encrypted with unique keys
- **Key Rotation**: Automatic key rotation when members leave groups
- **Secure Storage**: Hardware-backed encryption for sensitive data
- **Data Integrity**: Hash-based message integrity verification

### **🔍 Security Monitoring:**
- **Real-time Monitoring**: Continuous security event detection
- **Threat Analysis**: Pattern recognition for suspicious activities
- **Security Scanning**: Automated vulnerability assessments
- **Incident Response**: Structured security incident handling

---

## 🎯 Security Validation & Compliance

### **📝 Input Validation:**
- **Password Policies**: Configurable password strength requirements
- **Email Validation**: RFC-compliant email format checking
- **Username Validation**: Character restrictions and length limits
- **Real-time Feedback**: Immediate validation during user input

### **🔐 Encryption Standards:**
- **AES-256**: Industry-standard symmetric encryption
- **RSA Key Exchange**: Secure key distribution
- **PBKDF2**: Password-based key derivation
- **Secure Random**: Cryptographically secure random number generation

### **📊 Compliance Features:**
- **GDPR Compliance**: Data protection and privacy controls
- **Audit Trails**: Complete security event logging
- **Data Retention**: Configurable data lifecycle management
- **Privacy Controls**: User-controlled data sharing and deletion

---

## 🚨 Security Monitoring & Response

---

## 📋 **Security Summary**

### **🔐 Authentication & Authorization**
- **AuthenticationManager**: Firebase authentication integration
- **SecureAuthenticationManager**: Enhanced security validation
- **Session Management**: Secure token handling and validation

### **🔒 Encryption Components**
- **MessageEncryptionManager**: End-to-end message encryption (AES-256)
- **GroupKeyManager**: Group-specific encryption keys
- **EncryptedSharedPreferencesManager**: Secure local storage with MasterKey

### **🛡️ Network Security**
- **SSLPinningManager**: Certificate pinning with CustomTrustManager
- **Secure HTTP**: SSL/TLS connections with certificate validation
- **Network Monitoring**: Connection security validation

### **🔧 Security Utilities**
- **SecureConfigManager**: Secure configuration management
- **Security Validation**: Input validation and sanitization
- **Key Management**: Secure key generation, storage, and rotation

---

*Comprehensive security architecture with authentication, encryption, SSL pinning, and secure storage for protecting user data and communications.* 