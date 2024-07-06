package io.silv.oflchat.core.connection

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.core.model.ConnectionEntity.State
import io.silv.oflchat.core.model.ConnectionEntity.State.*
import io.silv.oflchat.core.model.toUpdate
import io.silv.oflchat.d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class EndpointDiscoveryHandler(
    private val connectionDao: ConnectionDao,
    private val scope: CoroutineScope,
) : EndpointDiscoveryCallback() {

    private val onLostTasks = ConcurrentHashMap<String, Job>()

    override fun onEndpointFound(eid: String, info: DiscoveredEndpointInfo) {
        onLostTasks[eid]?.cancel()
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
        onLostTasks[eid] = scope.launch {
            delay(3000)

            connectionDao.selectByEndpointId(eid)?.let { connection ->
                if (connection.status !in listOf(PENDING, SENT, ACCEPTED)) {
                    connectionDao.update(connection.toUpdate().copy(status = LOST))
                } else {
                    Timber.wtf("onEndpointLost called before disconnected")
                }
                Timber.d(connection.status.toString())
            }
        }
    }
}