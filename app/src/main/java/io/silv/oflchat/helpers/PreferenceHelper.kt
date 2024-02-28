package io.silv.oflchat.helpers

import io.silv.oflchat.OflChatApp
import io.silv.oflchat.preferences.DatastorePreferenceStore
import io.silv.oflchat.preferences.dataStore
import java.util.UUID

object PreferenceHelper {

    private val prefs by lazy {
        DatastorePreferenceStore(OflChatApp.instance.dataStore)
    }

    val username = prefs.getString("username", UUID.randomUUID().toString())

    val alwaysAdvertise = prefs.getBoolean("always_advertise", true)
}