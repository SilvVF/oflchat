package io.silv.oflchat.ui.screens

import android.widget.Toast
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.state_holders.permission.rememberMultiplePermissionsState
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

class PermissionsScreen : Screen {

    override val key: ScreenKey
        get() = UUID.randomUUID().toString()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val permissionState = rememberMultiplePermissionsState(
            permissions = OflChatApp.defaultPermissions
        )

        LaunchedEffect(Unit) {
            snapshotFlow { permissionState.allPermissionsGranted }
                .collectLatest { allGranted ->
                    if (allGranted && !navigator.pop()) {
                        navigator.push(ConversationsScreen)
                    }
            }
        }

        fun requestPermission() {
            try {
                permissionState.launchMultiplePermissionRequest()
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
