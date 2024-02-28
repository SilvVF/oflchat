package io.silv.oflchat.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.CollectEventsWithLifecycle
import io.silv.oflchat.viewmodels.DiscoverScreenModel
import java.util.UUID

class DiscoverScreen: Screen {

    override val key: ScreenKey
        get() = UUID.randomUUID().toString()

    @Composable
    override fun Content() {

        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val screenModel = rememberScreenModel { DiscoverScreenModel() }

        LifecycleEffect(
            onStarted = screenModel::discover,
            onDisposed = screenModel::stopDiscovery
        )

        CollectEventsWithLifecycle(screenModel) { event ->
            when(event) {
                is DiscoverScreenModel.DiscoverEvent.ShowSnackBar -> {
                    val result = snackbarHostState.showSnackbar(
                        context.getString(event.message, event.messageArgs)
                    )
                    event.onResult(result)
                }
                is DiscoverScreenModel.DiscoverEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        context.getString(event.message, event.messageArgs),
                        event.length
                    ).show()
                }
            }
        }

        DiscoverScreenContent(
            snackbarHostStateProvider = { snackbarHostState },
            endpointProvider = { screenModel.endpoints }
        )
    }
}

@Composable
private fun DiscoverScreenContent(
    snackbarHostStateProvider: () -> SnackbarHostState,
    endpointProvider: () -> List<ConnectionHelper.Endpoint>
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostStateProvider()) },
        topBar = {
            TopAppBar(title = { Text("Discover") })
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            LazyColumn {
                items(
                    items = endpointProvider(),
                    key = { endpoint -> endpoint.id }
                ) { endpoint ->
                    Text(endpoint.info.endpointName, Modifier.fillMaxWidth())
                }
            }
        }
    }
}