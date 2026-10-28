# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Media3 ExoPlayer hardware renderers and constructors for Android 6 compatibility
-keepclassmembers class androidx.media3.** { *; }
-dontwarn androidx.media3.**
