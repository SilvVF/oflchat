package io.silv.oflchat.core.model.transmit

import java.nio.ByteBuffer

data class ProtoData(
    val protoType: ProtoType,
    val byteArray: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProtoData

        if (protoType != other.protoType) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = protoType.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}

enum class ProtoType {
    Message,
    User,
    RTC;

    companion object {
        fun unwrap(byteArray: ByteArray): ProtoData {

            val buf = ByteBuffer.wrap(byteArray)

            val type = buf.getShort().toInt()

            return ProtoData(
                ProtoType.entries[type],
                ByteArray(buf.remaining()).also { buf.get(it) }
            )
        }
    }
}

private fun buf(dSize: Int) = ByteBuffer.allocate(Short.SIZE_BYTES + dSize)

fun ProtoType.wrap(byteArray: ByteArray): ByteArray {
    return buf(byteArray.size)
        .putShort(this.ordinal.toShort())
        .put(byteArray)
        .array()
}


//data class Packet(
//    val version: Int,
//    val type: PacketType,
//    val data: ByteArray
//): Codable<Packet> {
//
//    override fun decode(input: ByteArray): Packet = decoder(input)
//
//    override fun encode(): ByteArray = encoder()
//
//    companion object {
//
//        const val VERSION = 1
//
//        fun decoder(input: ByteArray): Packet {
//            return ByteArrayInputStream(input).source().buffer().use { source ->
//
//                val version = source.readInt().also { if (it != VERSION) error("Version Mismatch") }
//                val type = PacketType.entries[source.readInt()]
//
//                Packet(
//                    version = version,
//                    type = type,
//                    data = source.readByteArray()
//                )
//            }
//        }
//    }
//
//    enum class PacketType(
//        val decodable: Decodable<*>
//    ) {
//        String(
//            { input -> input.decodeToString() }
//        )
//    }
//}
//
//private fun Packet.encoder(): ByteArray {
//    val bos = ByteArrayOutputStream()
//
//    bos.sink().buffer().use { sink ->
//        sink.writeInt(this.version)
//        sink.writeInt(this.type.ordinal)
//        sink.write(this.data)
//    }
//
//    return bos.toByteArray()
//}





