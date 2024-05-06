package io.silv.oflchat.helpers

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import io.silv.oflchat.core.cache.AudioCache
import io.silv.oflchat.core.model.transmit.Packet
import io.silv.oflchat.core.model.transmit.Packet.PacketType
import io.silv.oflchat.core.model.transmit.Stream
import io.silv.oflchat.core.model.transmit.Stream.StreamType.AUDIO
import io.silv.oflchat.core.model.transmit.decodeType
import okio.buffer
import okio.source
import timber.log.Timber
import java.io.InputStream

object PayloadHelper {

    private val client: ConnectionsClient
        get() = ConnectionHelper.getClient()

    val callback = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val packet = Packet.decoder(payload.asBytes()!!)

                    when (packet.type) {
                        PacketType.String -> {
                            val data = packet.type.decodable.decodeType<String>(packet.data)
                        }
                    }

                    Timber.d("received packet [\n\t$packet\n]")
                }
                Payload.Type.STREAM -> {
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

    fun sendString(endpointId: String, data: String) {
        val encoded = data.encodeToByteArray()

        val bytes = Packet(
            version = Packet.VERSION,
            type = PacketType.String,
            data = encoded
        )
            .encode()

        send(endpointId, bytes)
    }


    fun stream(endpointId: String, inputStream: InputStream) {
        client.sendPayload(
            endpointId,
            Payload.fromStream(inputStream)
        )
    }
}