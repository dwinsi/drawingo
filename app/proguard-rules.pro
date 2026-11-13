# Drawingo ProGuard / R8 Rules

# Keep Room database and entities
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.drawingo.data.** { *; }

# Keep data models used with Gson serialization
-keep class com.example.drawingo.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Compose metadata
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
