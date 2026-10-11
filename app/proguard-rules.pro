# ProGuard / R8 Optimization Rules for Colorblock Art

# Preserve all app classes, widgets, and actions
-keep class com.simple.colorblockart.** { *; }

# Preserve Glance Widget Receivers and Action Callbacks
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }

# Preserve Firebase Messaging Service
-keep class * extends com.google.firebase.messaging.FirebaseMessagingService { *; }
-keep class com.google.firebase.messaging.** { *; }

# Preserve DataStore Preferences
-keep class androidx.datastore.preferences.core.** { *; }

# OkHttp & Coroutines
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
