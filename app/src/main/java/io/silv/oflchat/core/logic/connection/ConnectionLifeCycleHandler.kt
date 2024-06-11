package io.silv.oflchat.core.logic.connection

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_OK
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.core.model.ConnectionEntity.State
import io.silv.oflchat.core.model.toUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ConnectionLifeCycleHandler(
    private val connectionDao: ConnectionDao,
    private val scope: CoroutineScope,
    private val userInitiated: Boolean = false,
) : ConnectionLifecycleCallback() {

    override fun onConnectionInitiated(eid: String, info: ConnectionInfo) {
        scope.launch {
            val existing = connectionDao.selectByUserId(info.uuid)

            if (existing != null) {
                updateExistingConnection(existing, info)
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
            updateConnectionLost(eid)
        }
    }

    private suspend fun updateConnectionWithResolution(
        eid: String,
        resolution: ConnectionResolution
    ) {
        connectionDao.selectByEndpointId(eid)?.let {
            connectionDao.update(
                it.copy(
                    status = when(resolution.status.statusCode) {
                        STATUS_OK -> State.ACCEPTED
                        STATUS_CONNECTION_REJECTED -> State.IGNORED
                        else -> State.NOT_CONNECTED
                    },
                    lastUpdateDate = Clock.System.now(),
                ).toUpdate()
            )
        }
    }

    private suspend fun updateConnectionLost(eid: String) {
        connectionDao.selectByEndpointId(eid)?.let {
            connectionDao.update(
                it.copy(status = State.LOST).toUpdate()
            )
        }
    }

    private suspend fun updateExistingConnection(
        existing: ConnectionEntity,
        info: ConnectionInfo,
    ) {
        connectionDao.update(
            existing.copy(
                userId = info.uuid,
                username = info.name,
                status = if(userInitiated) State.SENT else State.PENDING,
                lastUpdateDate = Clock.System.now(),
            )
                .toUpdate()
        )
    }

    private suspend fun createConnection(
        eid: String,
        info: ConnectionInfo,
    ) {
        connectionDao.insert(
            ConnectionEntity(
                endpointId = eid,
                userId = info.uuid,
                username = info.name,
                status = if(userInitiated) State.SENT else State.PENDING,
                lastUpdateDate = Clock.System.now(),
                shouldNotify = true
            )
        )
    }
}