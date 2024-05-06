package io.silv.oflchat.core.model.transmit

import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

data class Packet(
    val version: Int,
    val type: PacketType,
    val data: ByteArray
): Codable<Packet> {

    override fun decode(input: ByteArray): Packet = decoder(input)

    override fun encode(): ByteArray = encoder()

    companion object {

        const val VERSION = 1

        fun decoder(input: ByteArray): Packet {
            return ByteArrayInputStream(input).source().buffer().use { source ->

                val version = source.readInt().also { if (it != VERSION) error("Version Mismatch") }
                val type = PacketType.entries[source.readInt()]

                Packet(
                    version = version,
                    type = type,
                    data = source.readByteArray()
                )
            }
        }
    }

    enum class PacketType(
        val decodable: Decodable<*>
    ) {
        String(
            { input -> input.decodeToString() }
        )
    }
}

private fun Packet.encoder(): ByteArray {
    val bos = ByteArrayOutputStream()

    bos.sink().buffer().use { sink ->
        sink.writeInt(this.version)
        sink.writeInt(this.type.ordinal)
        sink.write(this.data)
    }

    return bos.toByteArray()
}





