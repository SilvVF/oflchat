package io.silv.oflchat.helpers

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import io.silv.oflchat.Message
import io.silv.oflchat.RtcSignal
import io.silv.oflchat.User
import io.silv.oflchat.core.cache.AudioCache
import io.silv.oflchat.core.call.SignalingClient
import io.silv.oflchat.core.database.UserDao
import io.silv.oflchat.core.model.transmit.ProtoType
import io.silv.oflchat.core.model.transmit.ProtoType.*
import io.silv.oflchat.core.model.transmit.Stream
import io.silv.oflchat.core.model.transmit.Stream.StreamType.AUDIO
import io.silv.oflchat.core.model.transmit.wrap
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import timber.log.Timber
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

typealias Task = suspend () -> Unit

object PayloadHelper {

    private val client: ConnectionsClient
        get() = ConnectionHelper.getClient()

    private val userDao: UserDao
        get() = DatabaseHelper.userDao()

    private val signalingClients = ConcurrentHashMap<String, SignalingClient>()

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineName("EventQueueScope"))

    private val queue = Channel<Task>(Channel.UNLIMITED)
    private var taskExecutorJob: Job? = null

    init { start() }

    fun start() {
        taskExecutorJob = scope.launch {
            queue.receiveAsFlow().collect { task ->
                launch(scope.coroutineContext) { task() }
            }
        }
    }

    fun stop() {
        taskExecutorJob?.cancel()
    }

    val callback = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {

                    val (type, data) = ProtoType.unwrap(payload.asBytes()!!)

                    when(type) {
                        Message -> {
                            val message = Message.newBuilder()
                                .mergeFrom(data)
                                .build()
                            
                            Timber.d("received message [\t$message\t]")
                        }
                        User -> {
                            val user = User.newBuilder()
                                .mergeFrom(data)
                                .build()

                            queue.trySend {
                                val existing = userDao.getById(user.uuid)
                                if (existing != null) {
                                    userDao.update(user.uuid, user.name)
                                } else {
                                    userDao.insert(user.uuid, user.name)
                                }
                            }
                        }
                        RTC -> {
                            val signal = RtcSignal.newBuilder()
                                .mergeFrom(data)
                                .build()

                            signalingClients[id]?.handleCommand(signal.message)
                        }
                    }
                }
                Payload.Type.STREAM -> {
                   queue.trySend {
                       val stream = Stream.decode(payload.asStream()!!.asInputStream())
                       Timber.d("received packet [$stream]")
                       when (stream.type) {
                           AUDIO -> {
                               val data = stream.data.source().buffer()
                               val contactId = data.readUtf8Line()!!
                               val conversationId = data.readUtf8Line()!!
                               val messageId = data.readUtf8Line()!!

                               Timber.d("$contactId, $conversationId, $messageId")

                               AudioCache.setAudioFileToCache(
                                   contactId,
                                   conversationId,
                                   messageId,
                                   data.inputStream()
                               )
                           }
                       }
                   }
                }
                Payload.Type.FILE -> {
                    Unit
                }
            }
        }
        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {
            Timber.d("received update bytesTransferred: ${update.bytesTransferred}")
        }
    }

    private fun send(endpointId: String, byteArray: ByteArray) {
       client.sendPayload(
            endpointId,
            Payload.fromBytes(byteArray)
        )
    }

    fun sendRtcSignal(endpointId: String, data: String) {
        send(
            endpointId,
            ProtoType.RTC.wrap(
                RtcSignal.newBuilder()
                    .setMessage(data)
                    .build()
                    .toByteArray()
            )
        )
    }

    fun sendMessage(endpointId: String, data: String) {
        send(
            endpointId,
            ProtoType.Message.wrap(
                Message.newBuilder()
                    .setId(runBlocking { PreferenceHelper.uuid.get() })
                    .setContent(data)
                    .build()
                    .toByteArray()
            )
        )
    }


    fun stream(endpointId: String, inputStream: InputStream) {
        client.sendPayload(
            endpointId,
            Payload.fromStream(inputStream)
        )
    }
}