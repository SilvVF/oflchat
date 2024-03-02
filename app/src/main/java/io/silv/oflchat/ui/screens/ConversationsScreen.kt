package io.silv.oflchat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.silv.oflchat.R
import io.silv.oflchat.core.model.Conversation
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.components.ConversationsTopBar
import io.silv.oflchat.ui.components.ConversationsTopBarDefaults
import io.silv.oflchat.ui.components.FastScrollLazyColumn
import io.silv.oflchat.ui.components.conversationTestData
import io.silv.oflchat.ui.theme.OflchatTheme
import io.silv.oflchat.viewmodels.ConversationScreenModel
import kotlinx.coroutines.launch

object ConversationsScreen: Screen {

    @Composable
    override fun Content() {

        val snackbarHostState = remember { SnackbarHostState() }
        val screenModel = rememberScreenModel { ConversationScreenModel() }
        val navigator = LocalNavigator.currentOrThrow

        val conversations by screenModel.conversations.collectAsState()

        ConversationScreenContent(
            requestQueueProvider = { emptyList() },
            conversationProvider = { conversations },
            snackBarHostStateProvider = { snackbarHostState },
            navigateToConnections = { navigator.push(ConnectionsScreen) }
        )
    }
}

@Composable
private fun ConversationScreenContent(
    requestQueueProvider: () -> List<ConnectionHelper.ConnectionRequest>,
    conversationProvider: () -> List<Conversation>,
    snackBarHostStateProvider: () -> SnackbarHostState,
    navigateToConnections: () -> Unit
) {
    val scrollBehavior = ConversationsTopBarDefaults.scrollBehavior()
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostStateProvider()) },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToConnections,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sms,
                    contentDescription = stringResource(id = R.string.discover_tab_title)
                )
            }
        },
        topBar = {
            val text = stringResource(id = R.string.conversations_tab_title)
            
            ConversationsTopBar(
                iconExpanded = {
                    val painter = painterResource(id = R.drawable.ic_launcher_foreground)
                    ConversationsTopBarDefaults.IconExpanded(painter = painter)
                },
                iconCollapsed = {
                    val painter = painterResource(id = R.drawable.ic_launcher_foreground)
                    ConversationsTopBarDefaults.IconCollapsed(painter = painter)
                },
                scrollBehavior = scrollBehavior,
                titleExpanded = { ConversationsTopBarDefaults.TitleExpanded(text = text) },
                titleCollapsed = { ConversationsTopBarDefaults.TitleCollapsed(text = text) },
                actions = { 
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }
                    ConversationsTopBarDefaults.ProfileIcon(
                        model = null,
                        onClick = {}
                    )
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            val lazyListState = rememberLazyListState()
            val scrolled by remember {
                derivedStateOf { lazyListState.firstVisibleItemIndex >= 1 }
            }
            val scope = rememberCoroutineScope()

            FastScrollLazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                state = lazyListState
            ) {
                conversationTestData()
            }
            if (scrolled) {
                FilledIconButton(
                    modifier = Modifier
                        .padding(22.dp)
                        .size(50.dp)
                        .align(Alignment.BottomCenter),
                    onClick = {
                        scope.launch { lazyListState.animateScrollToItem(0) }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = stringResource(id = R.string.scroll_to_top)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ConversationScreenPreview() {
    OflchatTheme {
        val snackBarHostState = remember { SnackbarHostState() }
        ConversationScreenContent(
            requestQueueProvider = { emptyList() },
            conversationProvider = { emptyList() },
            snackBarHostStateProvider = { snackBarHostState },
            navigateToConnections = {}
        )
    }
}