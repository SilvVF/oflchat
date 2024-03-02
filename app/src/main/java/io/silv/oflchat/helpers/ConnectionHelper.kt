package io.silv.oflchat.helpers

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
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
import io.silv.oflchat.state_holders.PermissionState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

object ConnectionHelper {

    data class ConnectionRequest(
        val endpointId: String,
        val connectionInfo: ConnectionInfo
    )

    data class Endpoint(
        val id: String,
        val info: DiscoveredEndpointInfo
    )

    val connections = mutableStateMapOf<String, Int>()
    val endpoints = mutableStateMapOf<String, DiscoveredEndpointInfo>()


    private val permissionState by lazy { PermissionState(OflChatApp.instance) }
    private const val SERVICE_ID: String = "io.silv.oflchat"

    private val advertisingOptions by lazy {
        AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setConnectionType(ConnectionType.BALANCED)
            .build()
    }

    private val discoveryOptions by lazy {
        DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()
    }

    private lateinit var client: ConnectionsClient

    fun sendData(string: String, id: String) {
        client.sendPayload(id, Payload.fromBytes(string.toByteArray()))
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun listen(context: Context): Job = GlobalScope.launch {
        client = Nearby.getConnectionsClient(context)
        launch(Dispatchers.IO) {
            client.startAdvertising(
                "Pixel",
                SERVICE_ID,
                object : ConnectionLifecycleCallback() {
                    override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
                        client.acceptConnection(
                            id,
                            object : PayloadCallback() {
                                override fun onPayloadReceived(id: String, payload: Payload) {
                                    if (payload.type == Payload.Type.BYTES) {
                                        val receivedBytes = payload.asBytes()
                                        Timber.d("received bytes ${receivedBytes?.decodeToString()}")
                                    }
                                }
                                override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {
                                }
                            }
                        )
                    }
                    override fun onConnectionResult(id: String, resolution: ConnectionResolution) {
                        when (resolution.status.statusCode) {
                            ConnectionsStatusCodes.STATUS_OK -> {
                                connections[id] = ConnectionsStatusCodes.STATUS_OK
                            }
                            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                                Timber.d("$id STATUS_CONNECTION_REJECTED")
                                connections.remove(id)
                            }
                            ConnectionsStatusCodes.STATUS_ERROR -> {
                                Timber.d("$id STATUS_ERROR")
                                connections.remove(id)
                            }
                        }
                    }
                    override fun onDisconnected(id: String) {
                        connections.remove(id)
                    }
                },
                advertisingOptions
            )
            client.startDiscovery(
                "Pixel",
                object: EndpointDiscoveryCallback() {
                    override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
                        endpoints[id] = info
                    }
                    override fun onEndpointLost(id: String) {
                        endpoints.remove(id)
                    }
                },
                discoveryOptions
            )
        }
        launch(Dispatchers.IO) {
            snapshotFlow { connections }.collectLatest {
                while (true) {
                    it.forEach {
                        sendData("hello", it.key)
                    }
                    delay(10000)
                }
            }
        }
    }


    class PermissionsMissing: Exception("Missing Permissions")
    class AlreadyDiscovering: Exception("Already in discovering state")
    class AlreadyAdvertising: Exception("Already in advertising state")
}