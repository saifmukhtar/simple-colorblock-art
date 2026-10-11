package com.simple.colorblockart

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeartbeatFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val action = message.data["action"] ?: return
        
        when (action) {
            "heartbeat" -> {
                // Incoming heartbeat: High-power attention vibration (260ms) and light up red flame
                VibrationHelper.vibrate(applicationContext, durationMs = 260)
                setWidgetState(HeartbeatState.STATE_RECEIVED)
            }
            "acknowledge" -> {
                // Partner saw your heartbeat: Double gentle receipt buzz and return to black
                VibrationHelper.vibrate(applicationContext, pattern = longArrayOf(0, 120, 80, 120))
                setWidgetState(HeartbeatState.STATE_IDLE)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("HeartbeatFCM", "New Token: $token")
    }

    private fun setWidgetState(newState: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val glanceId = GlanceAppWidgetManager(applicationContext)
                    .getGlanceIds(HeartbeatWidget::class.java).firstOrNull()
                
                if (glanceId != null) {
                    updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        val mutablePrefs = prefs.toMutablePreferences()
                        mutablePrefs[HeartbeatState.WIDGET_STATE_KEY] = newState
                        mutablePrefs
                    }
                    HeartbeatWidget().update(applicationContext, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
