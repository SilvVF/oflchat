package io.silv.oflchat.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.firstOrNull

/**
 * Modified from https://github.com/aniyomiorg/aniyomi AndroidPreference
 * to use datastore instead of SharedPreferences
 */
class DatastorePreferenceStore(
    private val datastore: DataStore<Preferences>
) : PreferenceStore {

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return DatastorePreference.StringPrimitive(datastore, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return DatastorePreference.LongPrimitive(datastore, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return DatastorePreference.IntPrimitive(datastore, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return DatastorePreference.FloatPrimitive(datastore, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return DatastorePreference.BooleanPrimitive(datastore, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return DatastorePreference.StringSetPrimitive(datastore, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> {
        return DatastorePreference.ObjectPrimitive(
            datastore,
            key,
            defaultValue,
            serializer,
            deserializer
        )
    }

    override suspend fun getAll(): Map<Preferences.Key<*>, Any> {
        return datastore.data.firstOrNull()?.asMap() ?: emptyMap()
    }
}