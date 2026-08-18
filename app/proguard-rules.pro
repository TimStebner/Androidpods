# Safe to strip only verbose Android log calls from optimized release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Jetpack Glance & App Widgets (ensures RemoteViews composition & workers are retained in release builds)
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class androidx.glance.appwidget.** { *; }
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# DataStore Preferences
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# HiddenApiBypass (classic L2CAP reflection)
-keep class org.lsposed.hiddenapibypass.** { *; }
