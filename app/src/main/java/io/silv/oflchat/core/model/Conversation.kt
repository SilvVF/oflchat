package io.silv.oflchat.core.model

data class Conversation(
    val id: Long,
    val name: String,
    val owner: String,
    val participants: List<String>,
    val lastMessage: String?,
    val lastReceived: Long,
)
