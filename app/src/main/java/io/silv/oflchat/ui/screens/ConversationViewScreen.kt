package io.silv.oflchat.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.processing.SurfaceProcessorInternal
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.lifecycle.DisposableEffectIgnoringConfiguration
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.silv.oflchat.core.call.LocalWebRtcSessionManager
import io.silv.oflchat.core.call.SignalingClient
import io.silv.oflchat.core.call.StreamPeerConnectionFactory
import io.silv.oflchat.core.call.WebRtcSessionManagerImpl
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.helpers.MediaHelper
import io.silv.oflchat.helpers.PayloadHelper
import io.silv.oflchat.viewmodels.ConversationViewScreenModel
import kotlinx.coroutines.delay


data class ConversationViewScreen(
    val conversationId: Long,
    val endpoint: ConnectionEntity
): Screen {

    @Composable
    override fun Content() {

        val context = LocalContext.current
        val screenModel = rememberScreenModel {
            ConversationViewScreenModel(
                conversationId,
                endpoint,
                WebRtcSessionManagerImpl(
                    context,
                    SignalingClient(endpoint.endpointId),
                    StreamPeerConnectionFactory(context)
                )
            )
        }

        CompositionLocalProvider(
            LocalWebRtcSessionManager provides screenModel.sessionManager
        ) {
            ConversationView(screenModel = screenModel)
        }
    }
}


@Composable
fun ConversationView(
    modifier: Modifier = Modifier,
    screenModel: ConversationViewScreenModel
) {
    val recorder = screenModel.recorderState

    val conversation by screenModel.conversation.collectAsState()
    val context = LocalContext.current


    Box(modifier = Modifier.fillMaxSize()) {

        DisposableEffectIgnoringConfiguration(Unit) {
            onDispose { screenModel.recorderState.dispose() }
        }

        Column(Modifier.align(Alignment.Center)) {

            if (recorder.canInitialize) {
                RecorderUi(recorder = recorder)
            }

            val items by remember {
                derivedStateOf { screenModel.audioAttachments.toList() }
            }

            LazyColumn {
                item(key = "rtc-session-info") {
                    val manager = LocalWebRtcSessionManager.current
                    val sessionState by manager.signalingClient.sessionStateFlow.collectAsState()

                    Text(
                        text = sessionState.toString(),
                        Modifier.clickable { screenModel.startCall() })
                }
                items(items) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(it.name)
                        TextButton(onClick = { screenModel.sendAudio(it) }) {
                            Text("Send")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RecorderUi(
    recorder: MediaHelper.RecorderState,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (recorder.recording)
                recorder.stop()
            else
                recorder.start()
        },
        modifier = modifier
    ) {
        Text(
            text = if (recorder.recording) "Stop" else "Start"
        )
    }
}