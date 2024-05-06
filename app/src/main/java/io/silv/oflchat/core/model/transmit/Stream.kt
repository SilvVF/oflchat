package io.silv.oflchat.core.model.transmit

import io.silv.oflchat.helpers.PreferenceHelper
import okio.Buffer
import okio.buffer
import okio.source
import java.io.File
import java.io.InputStream
import java.util.UUID

data class Stream(
    val version: Int,
    val type: StreamType,
    val data: InputStream
) {


    enum class StreamType {
        AUDIO
    }

    fun encode(): InputStream {
        val source = data.source().buffer()

        val append = Buffer()

        append.writeInt(version)
        append.writeInt(type.ordinal)

        val combined = Buffer()

        combined.writeAll(append)
        combined.writeAll(source)

        return combined.inputStream()
    }

    companion object {

        suspend fun wrap(type: StreamType, file: File): InputStream {
            return file.source().buffer().use { source ->
                val append = Buffer()

                append
                    .writeInt(VERSION)
                    .writeInt(type.ordinal)
                    .writeUtf8("test").writeUtf8("\n")
                    .writeUtf8(PreferenceHelper.uuid.get()).writeUtf8("\n")
                    .writeUtf8(UUID.randomUUID().toString()).writeUtf8("\n")

                val combined = Buffer()

                combined.writeAll(append)
                combined.writeAll(source)

                combined.inputStream()
            }
        }

        const val VERSION = 1

        fun decode(input: InputStream): Stream {
            val source = input.source().buffer()

            return Stream(
                version = source.readInt(),
                type = StreamType.entries[source.readInt()],
                data = source.inputStream()
            )
        }
    }
}