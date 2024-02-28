package io.silv.oflchat.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface EventProducer <T> {
    suspend fun collectFromUi(collector: FlowCollector<T>)
    suspend fun emitEvent(event: T)
    fun tryEmitEvent(event: T): ChannelResult<Unit>

    companion object {

        fun <T> default(): EventProducer<T> = DefaultEventProducer()

        private class DefaultEventProducer<T>(): EventProducer<T> {

            private val eventChannel = Channel<T>(UNLIMITED)

            override fun tryEmitEvent(event: T): ChannelResult<Unit> {
                return eventChannel.trySend(event)
            }

            override suspend fun collectFromUi(collector: FlowCollector<T>)=
                withContext(Dispatchers.Main.immediate) { eventChannel.receiveAsFlow().collect(collector) }

            override suspend fun emitEvent(event: T) {
                withContext(Dispatchers.Main.immediate) {
                    eventChannel.send(event)
                }
            }
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun <T> CollectEventsWithLifecycle(producer: EventProducer<T>, collector: FlowCollector<T>) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            producer.collectFromUi(collector)
        }
    }
}