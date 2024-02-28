package io.silv.oflchat.preferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Preference<T> {

    fun key(): String

    suspend fun get(): T

    suspend fun set(value: T)

    suspend fun isSet(): Boolean

    suspend fun delete()

    fun defaultValue(): T

    fun changes(): Flow<T>

    fun stateIn(scope: CoroutineScope): StateFlow<T>
}
