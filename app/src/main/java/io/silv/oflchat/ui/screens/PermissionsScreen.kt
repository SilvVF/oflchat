package io.silv.oflchat.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityOptionsCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

object PermissionsScreen : Screen {
    
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val permissionState = rememberPermissionState(context)

        LaunchedEffect(Unit) {
            snapshotFlow { permissionState.allGranted }.collect { allGranted ->
                Log.d("permissions", allGranted.toString())
                Log.d("permissions", permissionState.permissionToState.toMap().toString())
                if (allGranted) { navigator.pop() }
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = permissionState::onResult
        )

        fun requestPermission() {
            try {
                permissionLauncher.launch(
                    PermissionState.defaultPermissions,
                    ActivityOptionsCompat.makeBasic()
                )
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Button(onClick = { requestPermission() }) {
                Text(text = "Accept")
            }
        }
    }
}

@Composable
private fun rememberPermissionState(
    context: Context = LocalContext.current
) = remember {
    PermissionState(context)
}

private class PermissionState(
    private val context: Context
) {

    val permissionToState = mutableStateMapOf<String, Boolean>()

    init {
        for (permission in defaultPermissions) {
            permissionToState[permission] =
                context.checkSelfPermission(permission) ==
                        PackageManager.PERMISSION_GRANTED
        }
    }

    val allGranted by derivedStateOf {
        (permissionToState.values
            .all { granted -> granted } && permissionToState.isNotEmpty()
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
            if (Build.VERSION.SDK_INT <= 31) {
                add(Manifest.permission.CHANGE_WIFI_STATE)
            }
            if (Build.VERSION.SDK_INT <= 30) {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
            }
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT <= 31) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add( Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if(Build.VERSION.SDK_INT >= 33) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
            .toTypedArray()
    }
}