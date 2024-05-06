package io.silv.oflchat.core.model.transmit

import okio.IOException

fun interface Decodable<T> {

    @Throws(IOException::class)
    fun decode(input: ByteArray): T
}

inline fun<reified T : Any> Decodable<*>.decodeType(input: ByteArray): T {
    return decode(input) as T
}

fun interface Encodable {

    @Throws(IOException::class)
    fun encode(): ByteArray
}

interface Codable<T>: Decodable<T>, Encodable