package io.silv.oflchat.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.helpers.DatabaseHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ConversationScreenModel: ScreenModel {

    val requests by derivedStateOf {
        ConnectionHelper.requestQueue.toList()
    }

    val conversations = DatabaseHelper.observeAllConversations()
        .stateIn(
            screenModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )
}