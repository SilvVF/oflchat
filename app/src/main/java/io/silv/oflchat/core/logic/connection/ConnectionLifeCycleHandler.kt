package io.silv.oflchat.core.logic.connection

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.core.model.ConnectionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ConnectionLifeCycleHandler(
    private val connectionDao: ConnectionDao,
    private val scope: CoroutineScope
) : ConnectionLifecycleCallback() {

    override fun onConnectionInitiated(eid: String, info: ConnectionInfo) {
        scope.launch {
            val existing = connectionDao.selectByEndpointId(eid)

            if (existing != null) {
                updateExistingConnection(existing, eid, info)
            } else {
                createConnection(eid, info)
            }
        }
    }

    override fun onConnectionResult(eid: String, resolution: ConnectionResolution) {
        scope.launch {
            updateConnectionWithResolution(eid, resolution)
        }
    }

    override fun onDisconnected(eid: String) {
        scope.launch {
            disconnectConnection(eid)
        }
    }

    private suspend fun disconnectConnection(eid: String) {

    }

    private suspend fun updateConnectionWithResolution(
        eid: String,
        resolution: ConnectionResolution
    ) {
        TODO()
    }

    private suspend fun updateExistingConnection(
        existing: ConnectionEntity,
        eid: String,
        info: ConnectionInfo,
    ) {
        TODO()
    }

    private suspend fun createConnection(
        eid: String,
        info: ConnectionInfo,
    ) {
        connectionDao.insertConnection(
            ConnectionEntity(
                endpointId = eid,
                userId = info.uuid,
                username = info.name,
                status = ConnectionEntity.State.NOT_CONNECTED,
                lastUpdateDate = Clock.System.now(),
                shouldNotify = true
            )
        )
    }
}