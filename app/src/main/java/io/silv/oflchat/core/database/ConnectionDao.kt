package io.silv.oflchat.core.database

import app.cash.sqldelight.EnumColumnAdapter
import io.silv.oflchat.core.DatabaseHandler
import io.silv.oflchat.core.model.ConnectionEntity
import io.silv.oflchat.core.model.ConnectionUpdate
import io.silv.oflchat.helpers.DatabaseHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class ConnectionDao(
    private val handler: DatabaseHandler
) {
    private val mapEntity = {
            endpoint_id: String,
            user_id: String,
            user_name: String,
            last_update_date: Instant,
            status: ConnectionEntity.State,
            should_notify: Boolean? ->

        ConnectionEntity(
            endpoint_id,
            user_id,
            user_name,
            last_update_date,
            status,
            should_notify ?: true
        )
    }

    suspend fun insert(connectionEntity: ConnectionEntity) {
        handler.await {
            connectionQueries.insert(
                endpoint_id = connectionEntity.endpointId,
                user_id = connectionEntity.userId,
                user_name = connectionEntity.username,
                last_update_date = connectionEntity.lastUpdateDate,
                status = connectionEntity.status,
                should_notify = connectionEntity.shouldNotify
            )
        }
    }

    suspend fun clear() = handler.await(true) { connectionQueries.clear() }

    suspend fun selectByUserId(uid: String): ConnectionEntity? {
        return handler.awaitOneOrNull {
            connectionQueries.selectByUserId(uid, mapEntity)
        }
    }

    suspend fun selectByEndpointId(eid: String): ConnectionEntity? {
        return handler.awaitOneOrNull {
            connectionQueries.selectByEndpointId(eid, mapEntity)
        }
    }

    fun observeAll(): Flow<List<ConnectionEntity>> {
        return handler.subscribeToList { connectionQueries.selectAll(mapEntity) }
    }

    suspend fun update(connectionUpdate: ConnectionUpdate) {
        handler.await {
            connectionQueries.update(
                connectionUpdate.userId,
                connectionUpdate.username,
                connectionUpdate.lastUpdateDate?.let { DatabaseHelper.InstantAdapter.encode(it) },
                connectionUpdate.status?.let { EnumColumnAdapter<ConnectionEntity.State>().encode(it) },
                connectionUpdate.shouldNotify,
                connectionUpdate.endpointId
            )
        }
    }
}