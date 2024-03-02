package io.silv.oflchat.helpers

import android.os.Build
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.preferences.DatastorePreferenceStore
import io.silv.oflchat.preferences.dataStore

object PreferenceHelper {

    private val prefs by lazy {
        DatastorePreferenceStore(OflChatApp.instance.dataStore)
    }

    val username = prefs.getString("username", "android_${Build.VERSION.BASE_OS}")

    val alwaysAdvertise = prefs.getBoolean("always_advertise", true)
}