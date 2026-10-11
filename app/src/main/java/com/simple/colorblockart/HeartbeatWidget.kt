package com.simple.colorblockart

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition

import androidx.compose.ui.unit.sp
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class HeartbeatWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val widgetState = prefs[HeartbeatState.WIDGET_STATE_KEY] ?: HeartbeatState.STATE_IDLE

            // Dynamic Core Colors:
            // Idle: Pure Black
            // Sent: Bright Light Green
            // Received: Bright Red with Glowing Halo
            val coreColor = when (widgetState) {
                HeartbeatState.STATE_RECEIVED -> Color(0xFFFF1744)     // Bright Electric Red Flame
                HeartbeatState.STATE_SENT_WAITING -> Color(0xFF00E676) // Bright Luminous Light Green
                else -> Color(0xFF111111)                              // Solid Black (Idle)
            }

            // Modern Expressionist Canvas 2x1 Art Tile
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(18.dp)
                    .clickable(onClick = actionRunCallback<WidgetClickAction>()),
                contentAlignment = Alignment.Center
            ) {
                // Background: Authentic Modern Expressionist Painting
                Image(
                    provider = ImageProvider(R.drawable.art_expressionist_background),
                    contentDescription = "Colorblock Art",
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(18.dp)
                )

                // Foreground: Floating Black Elements directly on the art
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Floating Concentric Black Rings with Living Core
                    Box(
                        modifier = GlanceModifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // The Concentric Black Vector Rings
                        Image(
                            provider = ImageProvider(R.drawable.concentric_black_rings),
                            contentDescription = null,
                            modifier = GlanceModifier.size(48.dp)
                        )

                        // Glowing Halo effect when active
                        if (widgetState == HeartbeatState.STATE_RECEIVED) {
                            Box(
                                modifier = GlanceModifier
                                    .size(24.dp)
                                    .cornerRadius(12.dp)
                                    .background(Color(0x55FF1744)) // Soft red radiant glow halo
                            ) {}
                        } else if (widgetState == HeartbeatState.STATE_SENT_WAITING) {
                            Box(
                                modifier = GlanceModifier
                                    .size(22.dp)
                                    .cornerRadius(11.dp)
                                    .background(Color(0x4400E676)) // Soft green radiant glow halo
                            ) {}
                        }

                        // The Living Core
                        Box(
                            modifier = GlanceModifier
                                .size(15.dp)
                                .cornerRadius(8.dp)
                                .background(coreColor)
                        ) {}
                    }

                    // Flexible Spacer
                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Center: 4 Floating Solid Black Dots directly on canvas
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(5.dp)
                                .cornerRadius(3.dp)
                                .background(Color(0xFF000000))
                        ) {}
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .size(5.dp)
                                .cornerRadius(3.dp)
                                .background(Color(0xFF000000))
                        ) {}
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .size(5.dp)
                                .cornerRadius(3.dp)
                                .background(Color(0xFF000000))
                        ) {}
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .size(5.dp)
                                .cornerRadius(3.dp)
                                .background(Color(0xFF000000))
                        ) {}
                    }

                    // Flexible Spacer
                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Right: Floating Monospace Hardware Code in Crisp Black
                    Text(
                        text = "[ 19 • S ]",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF000000)),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

object HeartbeatState {
    val WIDGET_STATE_KEY = stringPreferencesKey("widget_state")
    val TAP_COUNT_KEY = intPreferencesKey("tap_count")
    val LAST_TAP_TIME_KEY = longPreferencesKey("last_tap_time")
    val FIRST_TAP_TIME_KEY = longPreferencesKey("first_tap_time")

    const val STATE_IDLE = "idle"
    const val STATE_SENT_WAITING = "sent_waiting"
    const val STATE_RECEIVED = "received"
}
