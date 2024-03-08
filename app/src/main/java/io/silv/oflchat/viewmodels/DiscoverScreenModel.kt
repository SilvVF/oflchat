package io.silv.oflchat.viewmodels

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.ScreenModel
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.EventProducer
import timber.log.Timber


class DiscoverScreenModel : ScreenModel, EventProducer<DiscoverScreenModel.DiscoverEvent> by EventProducer.default() {

    val endpoints by derivedStateOf {
        Timber.d( ConnectionHelper.endpoints.entries.map { Pair(it.key, it.value) }.toString())
        ConnectionHelper.endpoints.entries.map { Pair(it.key, it.value) }
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