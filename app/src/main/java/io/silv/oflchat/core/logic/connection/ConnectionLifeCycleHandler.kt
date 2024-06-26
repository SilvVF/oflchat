package io.silv.oflchat.core.logic.connection

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.core.model.ConnectionEntity.State
import io.silv.oflchat.core.model.toUpdate
import io.silv.oflchat.helpers.PayloadHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
                connectionDao.update(
                    existing.copy(
                        endpointId = eid,
                        status = if(userInitiated) State.SENT else State.PENDING,
                        lastUpdateDate = Clock.System.now(),
                    )
                        .toUpdate()
                )
            } else {
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
    }

    override fun onConnectionResult(eid: String, resolution: ConnectionResolution) {
        scope.launch {
            val connection = connectionDao.selectByEndpointId(eid) ?: return@launch
            connectionDao.update(
                connection.copy(
                    status =   when (resolution.status.statusCode) {
                        ConnectionsStatusCodes.STATUS_OK -> State.ACCEPTED
                        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> State.IGNORED
                        ConnectionsStatusCodes.STATUS_ERROR -> State.NOT_CONNECTED
                        else -> State.NOT_CONNECTED
                    },
                    lastUpdateDate = Clock.System.now(),
                ).toUpdate()
            )

            if (resolution.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                var i = 0
                while (true) {
                    PayloadHelper.sendString(eid, "Test message $i")
                    i += 1
                    delay(3000)
                }
            }
        }
    }

    override fun onDisconnected(eid: String) {
        scope.launch {
            val connection = connectionDao.selectByEndpointId(eid) ?: return@launch

            connectionDao.update(
                connection.copy(
                    status =  State.NOT_CONNECTED
                )
                    .toUpdate()
            )
        }
    }
}