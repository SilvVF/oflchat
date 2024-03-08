package io.silv.oflchat.helpers

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionType
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.core.model.Contact
import io.silv.oflchat.core.model.toUpdate
import io.silv.oflchat.helpers.CStatus.Error
import io.silv.oflchat.helpers.CStatus.None
import io.silv.oflchat.helpers.CStatus.Ok
import io.silv.oflchat.helpers.CStatus.Rejected
import io.silv.oflchat.preferences.initialize
import io.silv.oflchat.state_holders.PermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class CStatus(val code: Int, statusString: () -> String?) {
    data object None: CStatus(-1, { null })
    data class Ok(private val string: () -> String?): CStatus(0, string)
    data class Rejected(private val string: () -> String?): CStatus(8004, string)
    data class Error(private val string: () -> String?): CStatus(13, string)

    val message by lazy { statusString() ?: "no status message" }
}

object ConnectionHelper {

    private lateinit var client: ConnectionsClient

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private const val SERVICE_ID: String = "io.silv.oflchat"

    private val advertisingOptions by lazy {
        AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(OflChatApp.isLowPower())
            .setConnectionType(ConnectionType.BALANCED)
            .build()
    }


    private val discoveryOptions by lazy {
        DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(OflChatApp.isLowPower())
            .build()
    }


    var discovering by mutableStateOf(false)
        private set

    var advertising by mutableStateOf(false)
        private set

    val endpoints = mutableStateMapOf<String, Endpoint>()

    private val permissionState by lazy { PermissionState(OflChatApp.instance, OflChatApp.defaultPermissions) }

    fun accpetConnection(endpointId: String) {
        if (!permissionState.checkAllGranted()) { return }
        client.acceptConnection(
            endpointId,
            object : PayloadCallback() {
                override fun onPayloadReceived(id: String, payload: Payload) {
                    if (payload.type == Payload.Type.BYTES) {
                        val receivedBytes = payload.asBytes()
                        Timber.d("received bytes ${receivedBytes?.decodeToString()}")
                    }
                }
                override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {
                    Timber.d("received update bytesTransferred: ${update.bytesTransferred}")
                }
            }
        )
    }

    suspend fun initiateConnection(endpointId: String) {
        if (!permissionState.checkAllGranted()) { return }
        client.requestConnection(
            getUserString(),
            endpointId,
            connectionLifecycleCallback
        )
    }

    private val connectionLifecycleCallback
        get() = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            scope.launch {
                Timber.d("Initiated $id")
                val prevEndpoint = endpoints[id]

                val endpoint = if (prevEndpoint != null) {
                    Timber.d("Updated $id")
                    prevEndpoint.copy(
                        conn = info,
                        contact = DatabaseHelper.insertOrUpdateContact(info.uuid, info.name),
                        found = true,
                    )
                } else {
                    val prev = endpoints.values.firstOrNull { it.contact.id == info.uuid }
                    if(prev != null) {
                        Timber.d("Removing endpoint with matching contact:${prev.contact}, $id")
                        endpoints.remove(prev.id)
                    }
                    Timber.d("Put $id")
                    Endpoint(
                        id = id,
                        conn = info,
                        contact = DatabaseHelper.insertOrUpdateContact(info.uuid, info.name),
                        found = true
                    )
                }

                if (endpoint.contact.acceptedOnce) {
                    accpetConnection(endpoint.id)
                    endpoints[id] = endpoint
                } else {
                    endpoints[id] = endpoint
                }
            }
        }
        override fun onConnectionResult(id: String, resolution: ConnectionResolution) {
            scope.launch {
                Timber.d("Result $id")
                val prevEndpoint = endpoints[id] ?: return@launch
                val endpoint = prevEndpoint.copy(
                    status = when (resolution.status.statusCode) {
                        ConnectionsStatusCodes.STATUS_OK -> Ok { resolution.status.statusMessage }
                        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Rejected { resolution.status.statusMessage }
                        ConnectionsStatusCodes.STATUS_ERROR -> Error { resolution.status.statusMessage }
                        else -> None
                    },
                )
                endpoints[id] = if(endpoint.connected) {
                    val updated = endpoint.contact.copy(acceptedOnce = true)
                    DatabaseHelper.updateContact(updated.toUpdate())
                    endpoint.copy(contact = updated)
                } else {
                    endpoint
                }
            }
        }
        override fun onDisconnected(id: String) {
            Timber.d("Disconnected $id")
            endpoints[id]?.let {
                endpoints[id] = it.copy(
                    conn = null,
                    status = None,
                    found = false
                )
            }
        }
    }


    private val endpointDiscoveryCallback
        get() = object: EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            scope.launch {
                Timber.d("Found $id")
                val prevEndpoint = endpoints[id]
                val endpoint = if (prevEndpoint == null) {
                    val prev = endpoints.values.firstOrNull { it.contact.id  == info.uuid }
                    if(prev != null) {
                        Timber.d("Removing endpoint with matching contact ${prev.contact}, $id")
                        endpoints.remove(prev.id)
                    }
                    Timber.d("Adding $id")
                    Endpoint(
                        id = id,
                        contact = DatabaseHelper.insertOrUpdateContact(info.uuid, info.name),
                        found = true
                    )
                } else {
                    Timber.d("Updating $id")
                    prevEndpoint.copy(
                        contact = DatabaseHelper.insertOrUpdateContact(info.uuid, info.name),
                        found = true
                    )
                }
                endpoints[id] = endpoint

                if (endpoint.contact.acceptedOnce) {
                    initiateConnection(endpoint.id)
                }
            }
        }
        override fun onEndpointLost(id: String) {
            Timber.d("Lost $id")
            endpoints[id]?.let {
                Timber.d("Updating $id")
                endpoints[id] = it.copy(found = false)
            }
        }
    }

    private suspend fun startAdvertising() {
        if (!permissionState.checkAllGranted() || advertising) { return }
        advertising = true
        Timber.d("Starting Advertising")
        client.startAdvertising(
            getUserString(),
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        )
    }

    private suspend fun getUserString() =
        PreferenceHelper.username.get() + "|" + PreferenceHelper.uuid.initialize()

    private fun startDiscovery() {

        if (!permissionState.checkAllGranted() || discovering) { return }
        discovering = true
        Timber.d("Starting Discovery")
        client.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        )
    }

    fun initialize(context: Context) {
        client = Nearby.getConnectionsClient(context)

        if (!permissionState.checkAllGranted()) { return }

        GlobalScope.launch {
            launch(Dispatchers.IO) { startAdvertising() }
            launch(Dispatchers.IO) { startDiscovery() }
        }
    }

    fun terminate() {
        client.stopAllEndpoints()
        client.stopDiscovery()
        client.stopAdvertising()
    }

    data class Endpoint(
        val id: String,
        val conn: ConnectionInfo? = null,
        val found: Boolean,
        val status: CStatus = None,
        val contact: Contact
    ) {
        val hasStatus = status != None
        val connected = status is Ok
    }
}

private val ConnectionInfo.uuid: String
    get() = endpointName.split("|").last()

private val ConnectionInfo.name: String
    get() = endpointName.split("|").first()

private val DiscoveredEndpointInfo.uuid: String
    get() = endpointName.split("|").last()

private val DiscoveredEndpointInfo.name: String
    get() = endpointName.split("|").first()