package io.silv.oflchat.viewmodels

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.R
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.EventProducer
import kotlinx.coroutines.launch


class DiscoverScreenModel : ScreenModel, EventProducer<DiscoverScreenModel.DiscoverEvent> by EventProducer.default() {

    val endpoints by derivedStateOf { ConnectionHelper.endpoints.toList() }

    fun discover() {
        screenModelScope.launch {
            ConnectionHelper.discover()
                .onSuccess {
                    emitEvent(
                        DiscoverEvent.ShowSnackBar(
                            message = R.string.discover_started,
                            duration = SnackbarDuration.Short,
                        )
                    )
                }
                .onFailure { throwable ->
                    emitEvent(
                        DiscoverEvent.ShowSnackBar(
                            message = R.string.discover_error,
                            messageArgs = listOf(throwable.localizedMessage ?: ""),
                            label = R.string.retry,
                            duration = SnackbarDuration.Indefinite,
                        ) {
                            when (it) {
                                SnackbarResult.Dismissed -> Unit
                                SnackbarResult.ActionPerformed -> discover()
                            }
                        }
                    )
                }
        }
    }


    fun stopDiscovery() {
        ConnectionHelper.stopDiscovery()
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