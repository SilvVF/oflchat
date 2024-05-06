package io.silv.oflchat.core.database

import io.silv.oflchat.core.DatabaseHandler
import io.silv.oflchat.core.model.ConnectionEntity

class ConnectionDao(
    private val handler: DatabaseHandler
) {

    suspend fun insertConnection(connectionEntity: ConnectionEntity) {
        TODO()
    }

    suspend fun selectByEndpointId(eid: String): ConnectionEntity? {
        TODO()
    }
}