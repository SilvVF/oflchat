package io.silv.oflchat.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.silv.oflchat.R
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.core.model.ConnectionEntity.State.ACCEPTED
import io.silv.oflchat.core.model.ConnectionEntity.State.BLOCKED
import io.silv.oflchat.core.model.ConnectionEntity.State.IGNORED
import io.silv.oflchat.core.model.ConnectionEntity.State.LOST
import io.silv.oflchat.core.model.ConnectionEntity.State.NOT_CONNECTED
import io.silv.oflchat.core.model.ConnectionEntity.State.PENDING
import io.silv.oflchat.core.model.ConnectionEntity.State.SENT
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.CollectEventsWithLifecycle
import io.silv.oflchat.ui.components.ConversationItemDefaults
import io.silv.oflchat.ui.components.ConversationsTopBarDefaults
import io.silv.oflchat.ui.components.FastScrollLazyColumn
import io.silv.oflchat.viewmodels.ConnectionsScreenModel

object ConnectionsScreen: Screen {

    @Composable
    override fun Content() {

        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val screenModel = rememberScreenModel { ConnectionsScreenModel() }
        val navigator = LocalNavigator.currentOrThrow
        val endpoints by screenModel.connectionsGroupedByFirstChar.collectAsState()

        CollectEventsWithLifecycle(screenModel) { event ->
            when(event) {
                is ConnectionsScreenModel.DiscoverEvent.ShowSnackBar -> {
                    val result = snackbarHostState.showSnackbar(
                        context.getString(event.message, event.messageArgs)
                    )
                    event.onResult(result)
                }
                is ConnectionsScreenModel.DiscoverEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        context.getString(event.message, event.messageArgs),
                        event.length
                    ).show()
                }
            }
        }

        ConnectionsScreenContent(
            snackbarHostStateProvider = { snackbarHostState },
            navigateBack = { navigator.pop() },
            initiateConnection =  screenModel::connect,
            navigateToConversation = {
                navigator.replace(
                    ConversationViewScreen(-1L, it)
                )
            },
            endpoints = endpoints
        )
    }
}

@Composable
private fun ConnectionsScreenContent(
    snackbarHostStateProvider: () -> SnackbarHostState,
    navigateBack: () -> Unit,
    initiateConnection: (id: String) -> Unit,
    navigateToConversation: (endpoint: String) -> Unit,
    endpoints: List<Pair<String, List<ConnectionEntity>>>,
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
        FastScrollLazyColumn(
            contentPadding = paddingValues
        ) {
            item("create-group") {
                Button(
                    onClick = { /*TODO*/ },
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    val label = stringResource(id = R.string.create_group)
                    Icon(imageVector = Icons.Outlined.GroupAdd, contentDescription = label)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = label)
                }
            }
            endpoints.fastForEach { (letter, endpoints) ->
                item(letter) {
                    Text(
                        text = remember(letter) { letter.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(horizontal = 2.dp + 25.dp)
                    )
                }
                items(
                    items = endpoints,
                ) {endpoint ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                initiateConnection(endpoint.endpointId)
                            }
                            .padding(8.dp)
                            .height(48.dp)
                            .animateContentSize()
                    ) {
                        Box(
                            Modifier
                                .drawWithContent {
                                    drawContent()
                                    val radius = size.width * 0.06f
                                    drawCircle(
                                        color = Color.Transparent,
                                        radius = radius,
                                        center = Offset(size.width - radius, size.height - radius)
                                    )
                                }
                        ) {
                            ConversationItemDefaults.ProfileIcon(
                                modifier = Modifier.size(48.dp),
                                text = endpoint.username,
                                textStyle = MaterialTheme.typography.headlineLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = endpoint.username,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = endpoint.userId,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.78f
                                    )
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = endpoint.endpointId,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.78f
                                    )
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        when(endpoint.status) {
                            NOT_CONNECTED -> { Text("NOT_CONNECTED") }
                            LOST -> { Text("LOST") }
                            PENDING -> {
                                ElevatedButton(
                                    onClick = { ConnectionHelper.accpetConnection(endpoint.endpointId) },
                                ) {
                                    Text("Accept")
                                }
                            }
                            SENT -> {
                                ElevatedButton(
                                    onClick = { ConnectionHelper.accpetConnection(endpoint.endpointId) },
                                ) {
                                    Text("Sent")
                                }
                            }
                            BLOCKED -> { Text("BLOCKED") }
                            IGNORED -> { Text("IGNORED") }
                            ACCEPTED ->{ Text("ACCEPTED") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionsSearchTopBar(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                var text by remember{ mutableStateOf("") }
                Text(
                    text = "To:",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(id = R.string.connection_search_placeholder)) },
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