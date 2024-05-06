package io.silv.oflchat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.lifecycle.DisposableEffectIgnoringConfiguration
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.silv.oflchat.helpers.MediaHelper
import io.silv.oflchat.viewmodels.ConversationViewScreenModel


data class ConversationViewScreen(
    val conversationId: Long,
    val endpointId: String,
): Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ConversationViewScreenModel(conversationId, endpointId) }
        val recorder = screenModel.recorderState

        val conversation = screenModel.conversation.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {

            DisposableEffectIgnoringConfiguration(Unit) {
                onDispose { screenModel.recorderState.dispose() }
            }

            Column(Modifier.align(Alignment.Center)) {
                Text("Conversation $conversation")

                if (recorder.canInitialize) {
                    RecorderUi(recorder = recorder)
                }

                val items by remember {
                    derivedStateOf { screenModel.audioAttachments.toList() }
                }

                LazyColumn {
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