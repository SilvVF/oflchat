package io.silv.oflchat

import android.app.Application
import android.os.PowerManager
import androidx.core.content.getSystemService

class OflChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: OflChatApp

        fun isLowPower(): Boolean {
            val powerManager = instance.getSystemService<PowerManager>()
            return powerManager?.isPowerSaveMode == true
        }
    }
}