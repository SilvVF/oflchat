package io.silv.oflchat.core.database

import io.silv.oflchat.core.DatabaseHandler
import io.silv.oflchat.core.model.ConnectionEntity

class UserDao(
    private val handler: DatabaseHandler,
) {
    val mapper = {
        id: String,
        name: String?,
        accent_id: Int,
        connection_status: ConnectionEntity. State,
        asset_id: String? ->
        Pair(id, name)
    }

    suspend fun getById(id: String): Pair<String, String?>? {
        return handler.awaitOneOrNull { userQueries.selectById(id, mapper) }
    }

    suspend fun insert(
        id: String,
        name: String,
    ) {
        handler.await {
            userQueries.insert(
                id = id,
                name = name,
                accentId = 0,
                connectionStatus = ConnectionEntity.State.ACCEPTED,
                assetId = null
            )
        }
    }

    suspend fun update(
        id: String,
        name: String,
    ) {
        handler.await {
            userQueries.insert(
                id = id,
                name = name,
                accentId = 0,
                connectionStatus = ConnectionEntity.State.ACCEPTED,
                assetId = null
            )
        }
    }
}