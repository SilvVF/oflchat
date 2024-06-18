package io.silv.oflchat.core.logic.connection

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.core.model.ConnectionEntity.State.LOST
import io.silv.oflchat.core.model.ConnectionEntity.State.NOT_CONNECTED
import io.silv.oflchat.core.model.toUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber

class EndpointDiscoveryHandler(
    private val connectionDao: ConnectionDao,
    private val scope: CoroutineScope,
) : EndpointDiscoveryCallback() {
    override fun onEndpointFound(eid: String, info: DiscoveredEndpointInfo) {
        scope.launch {
            val existing = connectionDao.selectByUserId(info.uuid)
            if (existing != null) {
                connectionDao.update(
                    existing.copy(
                        endpointId = eid,
                        lastUpdateDate = Clock.System.now(),
                        shouldNotify = true,
                        status = when(existing.status) {
                            NOT_CONNECTED, LOST -> NOT_CONNECTED
                            else -> existing.status
                        }
                    )
                        .toUpdate()
                )
            } else {
                connectionDao.insert(
                    ConnectionEntity(
                        endpointId = eid,
                        userId = info.uuid,
                        username = info.name,
                        status = NOT_CONNECTED,
                        lastUpdateDate = Clock.System.now(),
                        shouldNotify = true
                    )
                )
            }
        }
    }

    override fun onEndpointLost(eid: String) {
        scope.launch {
            connectionDao.selectByEndpointId(eid)?.let { connection ->
                Timber.d(connection.status.toString())
            }
        }
    }
}