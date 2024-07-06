package io.silv.oflchat.core.call

import android.content.Context
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWebRtcSessionManager: ProvidableCompositionLocal<WebRtcSessionManager?> =
    staticCompositionLocalOf { null }

