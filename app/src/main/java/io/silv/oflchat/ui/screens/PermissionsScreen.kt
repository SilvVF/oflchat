package io.silv.oflchat.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityOptionsCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.silv.oflchat.state_holders.PermissionState
import io.silv.oflchat.state_holders.rememberPermissionState
import java.util.UUID

class PermissionsScreen : Screen {

    override val key: ScreenKey
        get() = UUID.randomUUID().toString()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val permissionState = rememberPermissionState(context)

        LaunchedEffect(Unit) {
            snapshotFlow { permissionState.allGranted }.collect { allGranted ->
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
            Button(onClick = { requestPermission() }, modifier = Modifier.align(Alignment.Center)) {
                Text(text = "Accept")
            }
        }
    }
}
