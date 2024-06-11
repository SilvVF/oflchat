package io.silv.oflchat.viewmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.core.model.ConversationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ConversationScreenModel: ScreenModel {

    val conversations = flowOf(emptyList<ConversationEntity>())
        .stateIn(screenModelScope,SharingStarted.Lazily, emptyList())
}