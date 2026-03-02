# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools proguard configuration.

# ── Strip debug/verbose logs in release ──
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# ── Firebase ──
-keepattributes Signature
-keepattributes *Annotation*

# Keep Firestore model classes used with toObject()/data maps
-keep class com.gitfast.app.data.local.entity.** { *; }
-keep class com.gitfast.app.data.model.** { *; }

# ── Room ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# ── Hilt ──
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Compose ──
-dontwarn androidx.compose.**

# ── Google Play Services / Credential Manager ──
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-keep class androidx.credentials.** { *; }

# ── Health Connect ──
-keep class androidx.health.connect.** { *; }

# ── Kotlin serialization / reflection ──
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }

# ── Enums (used by Room converters) ──
-keepclassmembers enum com.gitfast.app.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
