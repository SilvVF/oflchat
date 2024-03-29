package io.silv.oflchat.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.silv.oflchat.R
import io.silv.oflchat.helpers.CStatus
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.CollectEventsWithLifecycle
import io.silv.oflchat.ui.components.ConversationsTopBarDefaults
import io.silv.oflchat.ui.components.FastScrollLazyColumn
import io.silv.oflchat.viewmodels.DiscoverScreenModel
import kotlinx.coroutines.launch
import java.util.UUID

object ConnectionsScreen: Screen {

    override val key: ScreenKey
        get() = UUID.randomUUID().toString()

    @Composable
    override fun Content() {

        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val screenModel = rememberScreenModel { DiscoverScreenModel() }
        val navigator = LocalNavigator.currentOrThrow

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
            navigateBack = { navigator.pop() },
            endpoints = screenModel.endpoints
        )
    }
}

@Composable
private fun DiscoverScreenContent(
    snackbarHostStateProvider: () -> SnackbarHostState,
    navigateBack: () -> Unit,
    endpoints: List<Pair<String, ConnectionHelper.Endpoint>>
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostStateProvider()) },
        topBar = {
            ConnectionsSearchTopBar {
                navigateBack()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        val scope = rememberCoroutineScope()
        FastScrollLazyColumn(
            contentPadding = paddingValues
        ) {
            items(
                items = endpoints,
                key = { (id, _) -> id }
            ) { (id, endpoint) ->
                Card(
                    onClick = {
                        scope.launch {
                            ConnectionHelper.initiateConnection(id)
                        }
                    },
                ) {
                    Text(text = endpoint.id)
                    Text(text = endpoint.contact.toString())
                    Text(text = endpoint.found.toString())
                    Text(endpoint.status.message)
                    endpoint.conn?.let {
                        if (!endpoint.connected) {
                            Button(onClick = { ConnectionHelper.accpetConnection(endpoint.id) }) {
                                val label = remember(endpoint.status) {
                                    when(endpoint.status) {
                                        is CStatus.Error, is CStatus.Rejected -> "Retry"
                                        CStatus.None, is CStatus.Ok -> "Accept"
                                    }
                                }
                                Text(label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionsSearchTopBar(
    onArrowBack: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)) {
        Column {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onArrowBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                title = {
                    ConversationsTopBarDefaults.TitleCollapsed(text = "New conversation")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "To:",
                    style = MaterialTheme.typography.bodySmall
                )
                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}