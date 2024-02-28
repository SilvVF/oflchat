package io.silv.oflchat.ui.tabs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.silv.oflchat.R
import io.silv.oflchat.core.model.Conversation
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.components.ConversationsTopBar
import io.silv.oflchat.ui.components.ConversationsTopBarDefaults
import io.silv.oflchat.ui.theme.OflchatTheme
import io.silv.oflchat.viewmodels.ConversationScreenModel

object ConversationsTab: Tab {

    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 0u,
            title = stringResource(R.string.conversations_tab_title),
            icon = rememberVectorPainter(image = Icons.AutoMirrored.Rounded.Message)
        )

    @Composable
    override fun Content() {

        val snackbarHostState = remember { SnackbarHostState() }
        val screenModel = rememberScreenModel { ConversationScreenModel() }

        val conversations by screenModel.conversations.collectAsState()

        ConversationScreenContent(
            requestQueueProvider = { screenModel.requests },
            conversationProvider = { conversations },
            snackBarHostStateProvider = { snackbarHostState }
        )
    }
}

@Composable
private fun ConversationScreenContent(
    requestQueueProvider: () -> List<ConnectionHelper.ConnectionRequest>,
    conversationProvider: () -> List<Conversation>,
    snackBarHostStateProvider: () -> SnackbarHostState
) {
    val scrollBehavior = ConversationsTopBarDefaults.scrollBehavior()
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostStateProvider()) },
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
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues
        ) {
            items(
                items = conversationProvider(),
                key = { conversation -> conversation.id }
            ) {conversation ->

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
            snackBarHostStateProvider = { snackBarHostState }
        )
    }
}