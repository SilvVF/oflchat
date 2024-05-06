package io.silv.oflchat.helpers

import android.os.Build
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.preferences.DatastorePreferenceStore
import io.silv.oflchat.preferences.dataStore
import io.silv.oflchat.preferences.initialize
import java.util.UUID

object PreferenceHelper {

    private val prefs by lazy {
        DatastorePreferenceStore(OflChatApp.instance.dataStore)
    }


    val username = prefs.getString("username", "android_${Build.VERSION.SDK_INT}")

    val uuid = prefs.getString("uuid", "${UUID.randomUUID()}")

    val alwaysAdvertise = prefs.getBoolean("always_advertise", true)

    suspend fun getUserString() = "${username.get()}|${uuid.initialize()}"
}
