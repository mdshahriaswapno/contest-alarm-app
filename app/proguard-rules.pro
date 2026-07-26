# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in 'proguard-android-optimize.txt' which is shipped with the Android SDK.

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }

# Kotlin Metadata (CRITICAL for Kotlin reflection with R8)
-keep class kotlin.Metadata { *; }

# Keep ALL data classes and API interfaces fully
-keep class com.greenchilli.contestalarm.data.** { *; }
-keepclassmembers class com.greenchilli.contestalarm.data.** { *; }
-keep interface com.greenchilli.contestalarm.data.api.** { *; }

# Keep all members of the Retrofit interfaces including return types
-keepclassmembers interface com.greenchilli.contestalarm.data.api.** {
    @retrofit2.http.* <methods>;
}

# Enum handling
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# General
-dontwarn javax.annotation.**
-keepattributes SourceFile,LineNumberTable

# Compose
-keep class androidx.compose.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
