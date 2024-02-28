package io.silv.oflchat.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionType
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.state_holders.PermissionState
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ConnectionHelper{

    data class ConnectionRequest(
        val endpointId: String,
        val connectionInfo: ConnectionInfo
    )

    data class Endpoint(
        val id: String,
        val info: DiscoveredEndpointInfo
    )

    val requestQueue = mutableStateListOf<ConnectionRequest>()
    val connections = mutableStateMapOf<String, ConnectionResolution>()

    val endpoints = mutableStateListOf<Endpoint>()
    private val permissionState by lazy { PermissionState(OflChatApp.instance) }

    var advertising by mutableStateOf(false)
        private set

    var discovering by mutableStateOf(false)
        private set

    private const val SERVICE_ID: String = "io.silv.oflchat"

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
        override fun onConnectionInitiated(
            endpointId: String,
            connectionInfo: ConnectionInfo
        ) {
            requestQueue.add(ConnectionRequest(endpointId, connectionInfo))
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            requestQueue.removeAll { it.endpointId == endpointId }
            connections[endpointId] = result
        }

        override fun onDisconnected(endpointId: String) {
            connections.remove(endpointId)
        }
    }

    suspend fun advertise(): Result<Boolean> {

        when {
            !permissionState.allGranted ->
                return Result.failure(PermissionsMissing())
            advertising ->
                return Result.failure(AlreadyAdvertising())
        }

        val username = PreferenceHelper.username.get()

        return suspendCoroutine { continuation ->
            client.startAdvertising(
                username,
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
                .addOnSuccessListener {
                    Timber.i("Started Advertising")
                    advertising = true
                    continuation.resume(Result.success(true))
                }
                .addOnFailureListener {
                    Timber.d(it)
                    advertising = false
                    continuation.resumeWith(Result.failure(it))
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

    suspend fun discover(): Result<Boolean> {

        when {
            !permissionState.allGranted ->
                return Result.failure(PermissionsMissing())
            discovering ->
                return Result.failure(AlreadyDiscovering())
        }

        return suspendCoroutine { continuation ->
            client.startDiscovery(
                SERVICE_ID,
                endpointCallback,
                discoveryOptions
            )
                .addOnSuccessListener {
                    Timber.i("Started discovery")
                    discovering = true
                    continuation.resume(Result.success(true))
                }
                .addOnFailureListener {
                    Timber.d(it)
                    discovering = false
                    continuation.resumeWith(Result.failure(it))
                }
        }
    }

    fun stopAdvertising() {
        client.stopAdvertising()
        advertising = false
    }

    fun stopDiscovery() {
        client.stopDiscovery()
        discovering = false
    }

    class PermissionsMissing: Exception("Missing Permissions")
    class AlreadyDiscovering: Exception("Already in discovering state")
    class AlreadyAdvertising: Exception("Already in advertising state")
}