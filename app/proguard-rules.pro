# Enhanced ProGuard rules for PartyMaker with security hardening
# ========================================================

# General configuration
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Security: Obfuscate package structure
-repackageclasses ''
-allowaccessmodification

# Security: Hide original source file names
-renamesourcefileattribute SourceFile

# Keep line numbers for crash reporting (but hide source files)
-keepattributes SourceFile,LineNumberTable

# ========== SECURITY CRITICAL - DO NOT MODIFY ==========
# Aggressively obfuscate security-related classes
-keep,allowobfuscation class com.example.partymaker.utils.security.** { *; }
-keep,allowobfuscation class com.example.partymaker.utils.auth.** { *; }

# Keep encrypted preferences working
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Keep Google API Client (for Tink)
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# Security: Remove sensitive log statements
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Security: Remove print statements
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# Security: Remove toString() that might expose sensitive data
-assumenosideeffects class java.lang.Object {
    public java.lang.String toString();
}

# Keep the application class and its methods
-keep public class com.example.partymaker.PartyApplication { *; }

# Keep model classes
-keep class com.example.partymaker.data.model.** { *; }

# Keep Firebase related classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Room Database classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# Keep Glide related classes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.**

# Keep Gson related classes
-keepattributes Signature
-keepattributes *Annotation*
#noinspection ShrinkerUnresolvedReference
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-dontwarn com.google.gson.**

# Keep AndroidX classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep Material Design classes
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep ViewHolder classes
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
   public <init>(android.view.View);
}

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# Keep LiveData classes
-keep class * extends androidx.lifecycle.LiveData {
    <init>();
}

# Keep annotation classes
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

# Keep lambda expressions
-dontwarn java.lang.invoke.**
-dontwarn **$$Lambda$*