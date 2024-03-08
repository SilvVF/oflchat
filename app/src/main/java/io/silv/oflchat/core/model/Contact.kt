package io.silv.oflchat.core.model

data class Contact(
    val id: String,
    val name: String,
    val acceptedOnce: Boolean = false
)

data class ContactUpdate(
    val id: String,
    val name: String?,
    val acceptedOnce: Boolean?
)

fun Contact.toUpdate(): ContactUpdate {
    return ContactUpdate(id, name, acceptedOnce)
}