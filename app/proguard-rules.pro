# Safe to strip only verbose Android log calls from optimized release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}
