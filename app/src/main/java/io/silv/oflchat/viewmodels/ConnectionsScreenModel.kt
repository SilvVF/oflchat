package io.silv.oflchat.viewmodels

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.helpers.DatabaseHelper
import io.silv.oflchat.ui.EventProducer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ConnectionsScreenModel(
    connectionDao: ConnectionDao = DatabaseHelper.connectionDao()
):
    ScreenModel,
    EventProducer<ConnectionsScreenModel.DiscoverEvent> by EventProducer.default() {

    val connectionsGroupedByFirstChar  = connectionDao.observeAll()
        .map { connections ->
            connections
                .groupBy { con -> con.username.first() }
                .mapKeys { firstLetter -> firstLetter.toString() }
                .mapValues { (_, cons) ->
                    cons.sortedBy { con -> con.username }
                }
                .toList()
        }
        .stateIn(
            screenModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun connect(id: String) {
        screenModelScope.launch {
            ConnectionHelper.initiateConnection(id)
        }
    }

    sealed interface DiscoverEvent {

        data class ShowToast(
            @StringRes val message: Int,
            val messageArgs: List<Any> = emptyList(),
            val length: Int = Toast.LENGTH_SHORT
        ): DiscoverEvent

        data class ShowSnackBar(
            @StringRes val message: Int,
            val messageArgs: List<Any> = emptyList(),
            @StringRes val label: Int? = null,
            val duration: SnackbarDuration = SnackbarDuration.Short,
            val onResult: (SnackbarResult) -> Unit = {}
        ): DiscoverEvent
    }
}