package com.simple.colorblockart

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HeartbeatWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = HeartbeatWidget()
}

class WidgetClickAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Automatically fetch and print the token to Logcat every time you tap the widget!
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("HeartbeatFCM", "MY DEVICE TOKEN: ${task.result}")
            }
        }
        
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val widgetState = prefs[HeartbeatState.WIDGET_STATE_KEY] ?: HeartbeatState.STATE_IDLE
            val currentTime = System.currentTimeMillis()
            val lastTapTime = prefs[HeartbeatState.LAST_TAP_TIME_KEY] ?: 0L
            val firstTapTime = prefs[HeartbeatState.FIRST_TAP_TIME_KEY] ?: 0L
            
            val mutablePrefs = prefs.toMutablePreferences()
            
            if (widgetState == HeartbeatState.STATE_RECEIVED) {
                // Heartbeat was received: A single tap acknowledges it
                mutablePrefs[HeartbeatState.WIDGET_STATE_KEY] = HeartbeatState.STATE_IDLE
                mutablePrefs[HeartbeatState.TAP_COUNT_KEY] = 0
                mutablePrefs[HeartbeatState.FIRST_TAP_TIME_KEY] = 0L
                mutablePrefs[HeartbeatState.LAST_TAP_TIME_KEY] = 0L
                
                // Satisfying single haptic thud on acknowledge
                VibrationHelper.vibrate(context, durationMs = 180)
                
                // Send acknowledgment back (fire & forget in background)
                sendWebhook("acknowledge")
                
            } else {
                // Not in received state: Multi-tap detection
                var tapCount = prefs[HeartbeatState.TAP_COUNT_KEY] ?: 0
                
                // Strict burst timing:
                // 1. Max 600ms allowed between consecutive taps (prevents separate 3-tap bursts from chaining)
                // 2. Entire sequence must complete within 1500ms from the very first tap
                if (currentTime - lastTapTime > 600 || currentTime - firstTapTime > 1500 || firstTapTime == 0L) {
                    tapCount = 1
                    mutablePrefs[HeartbeatState.FIRST_TAP_TIME_KEY] = currentTime
                } else {
                    tapCount += 1
                }
                
                if (tapCount == 3) {
                    // Send Heartbeat!
                    sendWebhook("heartbeat")
                    
                    // Put local widget in "Sent / Waiting" bright green state
                    mutablePrefs[HeartbeatState.WIDGET_STATE_KEY] = HeartbeatState.STATE_SENT_WAITING
                    
                    // Powerful haptic confirmation pulse
                    VibrationHelper.vibrate(context, durationMs = 220)
                } else if (tapCount == 7) {
                    // Secret Gadget Move (7 Rapid Taps): Copy token to clipboard
                    copyTokenToClipboard(context)
                    tapCount = 0 // Reset entirely
                    mutablePrefs[HeartbeatState.FIRST_TAP_TIME_KEY] = 0L
                }
                
                mutablePrefs[HeartbeatState.TAP_COUNT_KEY] = tapCount
                mutablePrefs[HeartbeatState.LAST_TAP_TIME_KEY] = currentTime
            }
            mutablePrefs
        }
        
        // Re-render the widget with the new state
        HeartbeatWidget().update(context, glanceId)
    }
    
    private fun copyTokenToClipboard(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Copy to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("FCM Token", task.result)
                clipboard.setPrimaryClip(clip)
                
                // Double powerful vibration for confirmation
                VibrationHelper.vibrate(context, pattern = longArrayOf(0, 150, 100, 150))
            }
        }
    }
    
    private fun sendWebhook(actionType: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = """{"action": "$actionType", "target": "${BuildConfig.TARGET_DEVICE}"}"""
                val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                
                val request = Request.Builder()
                    .url(BuildConfig.WORKER_URL)
                    .post(body)
                    .addHeader("Authorization", BuildConfig.AUTH_TOKEN)
                    .build()
                    
                client.newCall(request).execute().use { response ->
                    // Webhook sent
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
