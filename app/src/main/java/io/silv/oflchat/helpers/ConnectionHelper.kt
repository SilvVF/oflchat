package io.silv.oflchat.helpers

import androidx.compose.runtime.mutableStateListOf
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionType
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import io.silv.oflchat.OflChatApp
import java.sql.Connection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ConnectionHelper {

    data class Endpoint(
        val id: String,
        val info: DiscoveredEndpointInfo
    )

    val endpoints = mutableStateListOf<Endpoint>()

    private val SERVICE_ID: String = "io.silv.oflchat"

    private val advertisingOptions by lazy {
        AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setConnectionType(ConnectionType.BALANCED)
            .setLowPower(OflChatApp.isLowPower())
            .build()
    }

    private val discoveryOptions by lazy {
        DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(OflChatApp.isLowPower())
            .build()
    }


    private val client by lazy {
        Nearby.getConnectionsClient(OflChatApp.instance)
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            TODO("Not yet implemented")
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when(result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {}
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {}
                ConnectionsStatusCodes.STATUS_ERROR -> {}
            }
        }

        override fun onDisconnected(endpointId: String) {
            TODO("Not yet implemented")
        }

    }

    suspend fun advertise(): Boolean {
        return suspendCoroutine { continuation ->
            client.startAdvertising(
                "",
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
                .addOnSuccessListener {
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    private val endpointCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            endpoints.add(Endpoint(endpointId, info))
        }
        override fun onEndpointLost(endpointId: String) {
            val idxToRemove =  endpoints.indexOfFirst { it.id == endpointId }
            if (idxToRemove != -1) {
                endpoints.removeAt(idxToRemove)
            }
        }
    }

    suspend fun discover(): Boolean {
        return suspendCoroutine { continuation ->
            client.startDiscovery(
                SERVICE_ID,
                endpointCallback,
                discoveryOptions
            )
                .addOnSuccessListener {
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    fun stopAdvertising() {
        client.stopAdvertising()
    }

    fun stopDiscovery() {
        client.stopDiscovery()
    }
}