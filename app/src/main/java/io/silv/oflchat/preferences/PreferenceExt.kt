package io.silv.oflchat.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

inline fun <reified T : Enum<T>> PreferenceStore.getEnum(
    key: String,
    defaultValue: T,
): Preference<T> {
    return getObject(
        key = key,
        defaultValue = defaultValue,
        serializer = { it.name },
        deserializer = {
            try {
                enumValueOf(it)
            } catch (e: IllegalArgumentException) {
                defaultValue
            }
        }
    )
}

suspend inline fun <reified T, R : T> Preference<T>.getAndSet(crossinline block: (T) -> R) = set(
    block(get()),
)

suspend operator fun <T> Preference<Set<T>>.plusAssign(item: T) {
    set(get() + item)
}

suspend operator fun <T> Preference<Set<T>>.minusAssign(item: T) {
    set(get() - item)
}

suspend fun Preference<Boolean>.toggle(): Boolean {
    set(!get())
    return get()
}