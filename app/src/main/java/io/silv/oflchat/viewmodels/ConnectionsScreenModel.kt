package io.silv.oflchat.viewmodels

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.EventProducer
import kotlinx.coroutines.launch


class ConnectionsScreenModel :
    ScreenModel,
    EventProducer<ConnectionsScreenModel.DiscoverEvent> by EventProducer.default() {

    val endpointsGroupedFirstChar by derivedStateOf {
        emptyList<Pair<String, List<Pair<String, String>>>>()
    }

    fun connect(id: String) {
        screenModelScope.launch {
            ConnectionHelper.initiateConnection(id)
        }
    }

    sealed interface DiscoverEvent {

        data class ShowToast(
            @StringRes  val message: Int,
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