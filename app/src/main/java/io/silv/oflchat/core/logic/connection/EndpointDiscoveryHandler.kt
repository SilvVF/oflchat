package io.silv.oflchat.core.logic.connection

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import io.silv.oflchat.core.database.ConnectionDao
import kotlinx.coroutines.CoroutineScope

class EndpointDiscoveryHandler(
    private val connectionDao: ConnectionDao,
    private val scope: CoroutineScope,
) : EndpointDiscoveryCallback() {
    override fun onEndpointFound(eid: String, info: DiscoveredEndpointInfo) {
    }

    override fun onEndpointLost(eid: String) {

    }
}