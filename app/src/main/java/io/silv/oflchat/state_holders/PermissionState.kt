package io.silv.oflchat.state_holders

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import timber.log.Timber


class PermissionState(
    private val context: Context,
    private val permissions: List<String>
) {

    private val permissionToState = mutableStateMapOf<String, Boolean>()

    init {
        refresh()
    }

    val allGranted by derivedStateOf {
        (permissionToState.values
            .all { granted -> granted }
                && permissionToState.isNotEmpty()
        ) || permissions.isEmpty()
    }

    fun refresh() {
        for (permission in permissions) {
            permissionToState[permission] =
                context.checkSelfPermission(permission) ==
                        PackageManager.PERMISSION_GRANTED
        }
    }

    fun checkAllGranted(): Boolean {
        return permissions.all {
            val res = context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            permissionToState[it] = res
            res
        }.also {
            Timber.d(it.toString())
        }
    }
}