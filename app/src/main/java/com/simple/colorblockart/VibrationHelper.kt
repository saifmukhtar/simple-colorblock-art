package com.simple.colorblockart

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object VibrationHelper {

    /**
     * Powerful, universal vibration that bypasses Android's "Touch feedback disabled" mute
     * and forces full hardware motor amplitude.
     */
    fun vibrate(context: Context, durationMs: Long = 200, pattern: LongArray? = null) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrator.hasVibrator()) {
                Log.w("VibrationHelper", "Device reports no physical vibrator")
                return
            }

            // Android 15/16: Only use 255 if the hardware supports amplitude control, else DEFAULT_AMPLITUDE
            val amplitude = if (vibrator.hasAmplitudeControl()) 255 else VibrationEffect.DEFAULT_AMPLITUDE
            val effect = if (pattern != null) {
                VibrationEffect.createWaveform(pattern, -1)
            } else {
                VibrationEffect.createOneShot(durationMs, amplitude)
            }

            // Android 14, 15, and 16 standard for P2P heartbeats & communication
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val vibAttributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_NOTIFICATION)
                    .build()
                vibrator.vibrate(effect, vibAttributes)
            } else {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                    .build()
                vibrator.vibrate(effect, audioAttributes)
            }
        } catch (e: Exception) {
            Log.e("VibrationHelper", "Primary vibration failed, trying predefined haptic: ${e.message}", e)
            try {
                val fallbackVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fallbackVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    fallbackVibrator.vibrate(durationMs)
                }
            } catch (ignored: Exception) {}
        }
    }
}
