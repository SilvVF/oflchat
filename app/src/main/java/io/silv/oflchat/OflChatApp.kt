package io.silv.oflchat

import android.app.Application
import android.os.PowerManager
import androidx.core.content.getSystemService
import io.silv.oflchat.helpers.ConnectionHelper
import timber.log.Timber


class OflChatApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        instance = this
        ConnectionHelper.listen(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
        lateinit var instance: OflChatApp

        fun isLowPower(): Boolean {
            val powerManager = instance.getSystemService<PowerManager>()
            return powerManager?.isPowerSaveMode == true
        }
    }
}