package io.silv.oflchat.viewmodels

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.silv.oflchat.R
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.StringProvider
import kotlinx.coroutines.launch

class DiscoverScreenModel(
    stringProvider: StringProvider
): ScreenModel, StringProvider by stringProvider {

    val snackbarHostState = SnackbarHostState()

    fun discover() {
        screenModelScope.launch {
            ConnectionHelper.discover()
               .onFailure { throwable ->

                   val snackbarResult = snackbarHostState.showSnackbar(
                       message = getString(
                           R.string.discover_error,
                           throwable.localizedMessage ?: ""
                       ),
                       actionLabel = getString(R.string.retry),
                       duration = SnackbarDuration.Indefinite
                   )

                   when(snackbarResult) {
                       SnackbarResult.Dismissed -> Unit
                       SnackbarResult.ActionPerformed -> discover()
                   }
               }
        }
    }

    fun showMessage() {
        screenModelScope.launch {

            if (snackbarHostState.currentSnackbarData != null) {
                return@launch
            }

            val snackbarResult = snackbarHostState.showSnackbar(
                message = getString(R.string.discover_tab_title),
                actionLabel = getString(R.string.retry),
                duration = SnackbarDuration.Indefinite
            )

            when(snackbarResult) {
                SnackbarResult.Dismissed -> Unit
                SnackbarResult.ActionPerformed -> discover()
            }
        }
    }

    fun stopDiscovery() {
        ConnectionHelper.stopDiscovery()
    }

    override fun onDispose() {
        stopDiscovery()
    }
}
