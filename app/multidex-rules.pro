# MultiDex rules for PartyMaker - Essential classes for main dex
# =====================================================================
# This file ensures critical classes are in the main dex to avoid 
# ClassNotFoundException during app startup before MultiDex initialization

# ===== CORE APPLICATION CLASSES =====
# Keep the Application class in main dex (critical for startup)
-keep class com.example.partymaker.PartyApplication { *; }

# ===== MULTIDEX FRAMEWORK =====
# Keep MultiDex classes themselves in main dex
-keep class androidx.multidex.MultiDex { *; }
-keep class androidx.multidex.MultiDexApplication { *; }

# ===== FIREBASE CORE (Essential for startup) =====
# Keep essential Firebase classes in main dex to avoid startup issues
-keep class com.google.firebase.FirebaseApp { *; }
-keep class com.google.firebase.FirebaseOptions { *; }
-keep class com.google.firebase.FirebaseOptions$Builder { *; }
-keep class com.google.firebase.provider.FirebaseInitProvider { *; }

# Firebase Auth core classes (needed for immediate auth check)
-keep class com.google.firebase.auth.FirebaseAuth { *; }
-keep class com.google.firebase.auth.FirebaseUser { *; }

# Firebase Database core classes (for immediate database access)
-keep class com.google.firebase.database.FirebaseDatabase { *; }
-keep class com.google.firebase.database.DatabaseReference { *; }

# ===== ANDROID CORE CLASSES =====
# Keep essential Android framework classes
-keep class android.app.Application { *; }
-keep class androidx.core.content.ContextCompat { *; }
-keep class androidx.appcompat.app.AppCompatActivity { *; }

# ===== SPLASH AND MAIN ACTIVITY =====
# Keep startup activities in main dex for immediate access
-keep class com.example.partymaker.ui.features.core.SplashActivity { *; }
-keep class com.example.partymaker.ui.features.core.MainActivity { *; }
-keep class com.example.partymaker.ui.features.auth.IntroActivity { *; }

# ===== ESSENTIAL UTILS =====
# Keep critical utility classes that are used during startup
-keep class com.example.partymaker.utils.auth.AuthenticationManager { *; }
-keep class com.example.partymaker.utils.core.AppConstants { *; }
-keep class com.example.partymaker.data.firebase.FirebaseAccessManager { *; }

# ===== NETWORKING ESSENTIALS =====
# Keep network manager for immediate connectivity checks
-keep class com.example.partymaker.data.api.NetworkManager { *; }
-keep class com.example.partymaker.data.api.ConnectivityManager { *; }

# ===== SECURITY CORE =====
# Keep essential security classes for immediate security initialization
-keep class com.example.partymaker.utils.security.core.SecureConfigManager { *; }
-keep class androidx.security.crypto.EncryptedSharedPreferences { *; }
-keep class androidx.security.crypto.MasterKey { *; }

# ===== VIEW MODELS (Core) =====
# Keep essential ViewModels for startup screens
-keep class com.example.partymaker.viewmodel.core.SplashViewModel { *; }
-keep class com.example.partymaker.viewmodel.core.MainActivityViewModel { *; }
-keep class com.example.partymaker.viewmodel.auth.IntroViewModel { *; }

# ===== REPOSITORY CORE =====
# Keep essential data repositories
-keep class com.example.partymaker.data.repository.UserRepository { *; }
-keep class com.example.partymaker.data.repository.GroupRepository { *; }

# ===== ROOM DATABASE CORE =====
# Keep Room database essentials for immediate local data access
-keep class com.example.partymaker.data.local.AppDatabase { *; }
-keep class androidx.room.Room { *; }
-keep class androidx.room.RoomDatabase { *; }

# ===== LIFECYCLE COMPONENTS =====
# Keep Android Architecture Components for ViewModels
-keep class androidx.lifecycle.ViewModelProvider { *; }
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }
-keep class androidx.lifecycle.ViewModelStore { *; }

# ===== MEMORY MANAGEMENT =====
# Keep memory manager for immediate memory optimization
-keep class com.example.partymaker.utils.infrastructure.system.MemoryManager { *; }

# ===== GOOGLE SERVICES =====
# Keep Google Services for immediate initialization
-keep class com.google.android.gms.common.GooglePlayServicesUtil { *; }
-keep class com.google.android.gms.common.ConnectionResult { *; }
-keep class com.google.android.gms.common.api.GoogleApiClient { *; }

# ===== KOTLIN ESSENTIALS =====
# Keep Kotlin runtime essentials
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# ===== PROGUARD OPTIMIZATION HINTS =====
# Suggest keeping these classes together for better dex optimization
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep classes annotated with @Keep in main dex
-keep @androidx.annotation.Keep class * { *; }