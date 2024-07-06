package io.silv.oflchat.core.model

import kotlinx.datetime.Instant
import java.io.Serializable

data class MemberEntity(
    val user: String,
    val role: Role
) {
    sealed class Role {
        data object Member : Role()
        data object Admin : Role()
        data class Unknown(val name: String) : Role()
    }
}

data class MessageEntity(val id: String) {
    enum class Status {
        PENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    enum class ContentType {
        TEXT, ASSET
    }
}

data class ConversationEntity(
    val id: Long,
    val owner: String?,
    val name: String,
    val multi: Boolean,
    val lastMessage: String?,
    val lastReceived: Long,
) {

    enum class Type { SELF, ONE_ON_ONE, GROUP, CONNECTION_PENDING }

    enum class MutedStatus { ALL_ALLOWED, ONLY_MENTIONS_AND_REPLIES_ALLOWED, MENTIONS_MUTED, ALL_MUTED }

    enum class ReceiptMode { DISABLED, ENABLED }
}


data class ConnectionUpdate(
    val endpointId: String,
    val userId: String? = null,
    val username: String? = null,
    val lastUpdateDate: Instant? = null,
    val status: ConnectionEntity.State? = null,
    val shouldNotify: Boolean? = null
)

fun ConnectionEntity.toUpdate(): ConnectionUpdate {
    return ConnectionUpdate(
        endpointId = endpointId,
        userId = userId,
        username = username,
        lastUpdateDate = lastUpdateDate,
        status = status,
        shouldNotify = shouldNotify
    )
}

data class ConnectionEntity(
    val endpointId: String,
    val userId: String,
    val username: String,
    val lastUpdateDate: Instant,
    val status: State,
    val shouldNotify: Boolean,
): Serializable {

    enum class State {
        /** Default - No connection state */
        NOT_CONNECTED,

        /** out of range or user disconnected **/
        LOST,

        /** The other user has sent a connection request to this one */
        PENDING,

        /** This user has sent a connection request to another user */
        SENT,

        /** The user has been blocked */
        BLOCKED,

        /** The connection has been ignored */
        IGNORED,

        /** The connection is complete and the conversation is in its normal state */
        ACCEPTED
    }
}

