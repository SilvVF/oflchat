package io.silv.oflchat.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import io.silv.oflchat.helpers.ConnectionHelper
import kotlinx.coroutines.launch

class DiscoverViewModel: ViewModel() {

    fun discover() {
        viewModelScope.launch {
            ConnectionHelper.discover()
        }
    }



    override fun onCleared() {
        super.onCleared()
        ConnectionHelper.stopDiscovery()
    }
}
