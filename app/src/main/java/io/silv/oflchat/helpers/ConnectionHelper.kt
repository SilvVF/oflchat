package io.silv.oflchat.helpers

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Strategy
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.applicationContext
import io.silv.oflchat.core.connection.ConnectionLifeCycleHandler
import io.silv.oflchat.core.connection.EndpointDiscoveryHandler
import io.silv.oflchat.state_holders.PermissionState
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

object ConnectionHelper: DefaultLifecycleObserver {

    private lateinit var client: ConnectionsClient

    fun getClient() = client

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("io.silv.ConnectionHelper"))

    private const val SERVICE_ID: String = "io.silv.oflchat"

    private val advertisingOptions by lazy {
        AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(OflChatApp.isLowPower())
            .build()
    }


    private val discoveryOptions by lazy {
        DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .setLowPower(OflChatApp.isLowPower())
            .build()
    }

    private var discovering by mutableStateOf(false)

    private var advertising by mutableStateOf(false)

    private val permissionState by lazy { PermissionState(applicationContext, OflChatApp.connectionPermissions) }

    fun accpetConnection(endpointId: String) {
        if (!permissionState.checkAllGranted()) { return }
        client.acceptConnection(
            endpointId,
            PayloadHelper.callback
        )
    }

    suspend fun initiateConnection(endpointId: String) {

        if (!permissionState.checkAllGranted()) { return }
        Timber.d("Trying to connect")
        client.requestConnection(
            PreferenceHelper.getUserString(),
            endpointId,
            ConnectionLifeCycleHandler(
                DatabaseHelper.connectionDao(),
                scope,
                userInitiated = true
            )
        )

        accpetConnection(endpointId)
    }

    private val connectionLifecycleCallback by lazy {
        ConnectionLifeCycleHandler(DatabaseHelper.connectionDao(), scope)
    }


    private val endpointDiscoveryCallback by lazy {
        EndpointDiscoveryHandler(DatabaseHelper.connectionDao(), scope)
    }

    private fun startAdvertising() {
        if (!permissionState.checkAllGranted() || advertising) { return }
        Timber.d("Starting Advertising")
        advertising = true
        client.startAdvertising(
            runBlocking { PreferenceHelper.getUserString() },
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        )
    }

    private fun startDiscovery() {
        if (!permissionState.checkAllGranted() || discovering) { return }
        Timber.d("Starting Discovery")
        discovering = true
        client.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        )
    }

    private fun stopDiscovery() {
        Timber.d("Stopping Discovery")
        discovering = false
        client.stopDiscovery()
    }

    fun initialize(context: Context) {
        Timber.d("Initializing ConnectionHelper")
        client = Nearby.getConnectionsClient(context)
        scope.launch {
            DatabaseHelper.connectionDao().clear()

            startAdvertising()
            startDiscovery()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        startAdvertising()
        startDiscovery()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopDiscovery()
        client.stopAdvertising()
    }

    fun terminate() {
        client.stopAllEndpoints()
        stopDiscovery()
        client.stopAdvertising()
    }
}
