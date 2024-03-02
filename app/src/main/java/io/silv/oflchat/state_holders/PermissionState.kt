package io.silv.oflchat.state_holders

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberPermissionState(
    context: Context = LocalContext.current
) = remember {
    PermissionState(context)
}

class PermissionState(context: Context) {

    private val permissionToState = mutableStateMapOf<String, Boolean>()

    init {
        for (permission in defaultPermissions) {
            permissionToState[permission] =
                context.checkSelfPermission(permission) ==
                        PackageManager.PERMISSION_GRANTED
        }
    }

    val allGranted by derivedStateOf {
        (permissionToState.values
            .all { granted -> granted }
                && permissionToState.isNotEmpty()
        ) || defaultPermissions.isEmpty()
    }

    fun onResult(results: Map<String, Boolean>) {
        for ((permission, granted) in results) {
            permissionToState[permission] = granted
        }
    }

    companion object {
        val defaultPermissions = buildList {
            add(Manifest.permission.ACCESS_WIFI_STATE)
            add(Manifest.permission.CHANGE_WIFI_STATE)
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= 31) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (Build.VERSION.SDK_INT >= 33) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
            .toTypedArray()
    }
}