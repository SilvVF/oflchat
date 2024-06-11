package io.silv.oflchat.state_holders

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber


class PermissionState(
    private val context: Context,
    private val permissions: List<String>
) {

    fun checkAllGranted(): Boolean {
        return permissions.all {
            val res = context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            res
        }.also {
            Timber.d(it.toString())
        }
    }
}