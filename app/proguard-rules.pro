# ProGuard rules for DailyCheckIn

# Keep Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep the app classes
-keep class com.dailycheckin.** { *; }
-keepclassmembers class com.dailycheckin.** { *; }
