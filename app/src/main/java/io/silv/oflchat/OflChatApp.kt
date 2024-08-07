package io.silv.oflchat

import android.Manifest
import android.app.Application
import android.os.Build
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
        ConnectionHelper.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ConnectionHelper.terminate()
    }

    companion object {
        lateinit var instance: OflChatApp

        val permissions: List<String> by lazy { mediaPermissions + connectionPermissions }

        fun isLowPower(): Boolean {
            val powerManager = instance.getSystemService<PowerManager>()
            return powerManager?.isPowerSaveMode == true
        }

        val mediaPermissions by lazy {
            listOf(
                Manifest.permission.RECORD_AUDIO
            )
        }

        val connectionPermissions by lazy {
            buildList {
                add(Manifest.permission.ACCESS_WIFI_STATE)
                add(Manifest.permission.CHANGE_WIFI_STATE)
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.CAMERA)
                add(Manifest.permission.RECORD_AUDIO)

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

                if (Build.VERSION.SDK_INT >= 31) {
                    add(Manifest.permission.BLUETOOTH_ADVERTISE)
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                    add(Manifest.permission.BLUETOOTH_SCAN)
                }
                if (Build.VERSION.SDK_INT >= 33) {
                    add(Manifest.permission.NEARBY_WIFI_DEVICES)
                }
            }
        }
    }
}