package io.silv.oflchat.viewmodels

import androidx.compose.runtime.mutableStateListOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.core.cache.AudioCache
import io.silv.oflchat.core.model.ConversationEntity
import io.silv.oflchat.core.model.transmit.Stream
import io.silv.oflchat.helpers.MediaHelper
import io.silv.oflchat.helpers.PayloadHelper
import io.silv.oflchat.ui.EventProducer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class ConversationViewScreenModel(
    private val conversationId: Long,
    private val endpointId: String,
): ScreenModel, EventProducer<Unit> by EventProducer.default() {

    val audioAttachments = mutableStateListOf<File>()

    val recorderState = MediaHelper.RecorderState()

    init {
        recorderState.events.onEach { event ->
            when(event) {
                is MediaHelper.RecorderEvent.AudioCreated -> {
                    audioAttachments.add(event.file)
                }
                // ignore collected in ui
                is MediaHelper.RecorderEvent.Error -> Timber.d(event.toString())
            }
        }
            .launchIn(screenModelScope)
    }

    val conversation = flowOf(emptyList<ConversationEntity>())
        .stateIn(
            screenModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    val audioFiles =  AudioCache.changes.receiveAsFlow().onStart { emit(Unit) }
        .map {
            AudioCache.getConversationDirectory("test")
                .listFiles()?.flatMap { it.listFiles().orEmpty().toList() }
                .orEmpty()
                .toList()
        }
        .stateIn(
            screenModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun sendAudio(file: File) {
        screenModelScope.launch {
            Stream.wrap(Stream.StreamType.AUDIO, file).use {
                PayloadHelper.stream(
                    endpointId = endpointId,
                    inputStream = it
                )
            }
        }
    }

    override fun onDispose() {
        super.onDispose()
        recorderState.dispose()
        audioAttachments.forEach {
            it.deleteRecursively()
        }
    }
}