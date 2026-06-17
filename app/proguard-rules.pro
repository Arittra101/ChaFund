# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================
# Stack trace readability
# ============================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Timber — strip verbose/debug logs in release
# ============================================================
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
}

# ============================================================
# Room — keep entity and DAO classes
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# ============================================================
# Koin — keep classes used by reflection
# ============================================================
-keepnames class * extends org.koin.core.module.Module

# ============================================================
# kotlinx.serialization
# ============================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.* {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}

# ============================================================
# Coroutines
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}