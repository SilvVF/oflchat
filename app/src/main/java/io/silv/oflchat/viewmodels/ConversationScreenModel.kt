package io.silv.oflchat.viewmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.helpers.DatabaseHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ConversationScreenModel: ScreenModel {

    val conversations = DatabaseHelper.observeAllConversations()
        .stateIn(
            screenModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )
}