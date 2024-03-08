package io.silv.oflchat.helpers

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.silv.ConversationEntity
import io.silv.Database
import io.silv.oflchat.OflChatApp
import io.silv.oflchat.core.AndroidDatabaseHandler
import io.silv.oflchat.core.DatabaseHandler
import io.silv.oflchat.core.model.Contact
import io.silv.oflchat.core.model.ContactUpdate
import io.silv.oflchat.core.model.Conversation
import io.silv.oflchat.core.model.toUpdate
import kotlinx.coroutines.flow.Flow

object DatabaseHelper {

    private val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            if (databaseValue.isEmpty()) {
                listOf()
            } else {
                databaseValue.split(",")
            }
        override fun encode(value: List<String>) = value.joinToString(separator = ",")
    }

    private const val DB_NAME = "oflchat.db"

    private val handler by lazy<DatabaseHandler>(LazyThreadSafetyMode.SYNCHRONIZED) {
        val driver =  AndroidSqliteDriver(
            schema = Database.Schema,
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            },
            context = OflChatApp.instance,
            name = DB_NAME
        )
        AndroidDatabaseHandler(
            db = Database(
                driver = driver,
                conversationEntityAdapter = ConversationEntity.Adapter(listOfStringsAdapter)
            ),
            driver = driver
        )
    }

    suspend fun getAllConversations(): List<Conversation> {
        return handler.awaitList { conversationQueries.selectAll(ConversationMapper::mapConversation) }
    }

    fun observeAllConversations(): Flow<List<Conversation>> {
        return handler.subscribeToList { conversationQueries.selectAll(ConversationMapper::mapConversation) }
    }

    suspend fun getContactById(id: String): Contact? {
        return handler.awaitOneOrNull { contactQueries.selectById(id, ContactMapper::mapContact) }
    }

    suspend fun insertContact(contact: Contact) {
        return handler.await { contactQueries.insert(contact.id, contact.name) }
    }

    suspend fun updateContact(update: ContactUpdate) {
        return handler.await {
            contactQueries.update(update.name, update.acceptedOnce, update.id)
        }
    }

    suspend fun insertOrUpdateContact(id: String, username: String): Contact {
        val prev = getContactById(id)
        return if (prev != null) {
            updateContact(prev.toUpdate().copy(name = username))
            prev.copy(name = username)
        } else {
            val contact = Contact(id, username)
            insertContact(contact)
            contact
        }
    }
}

private object ConversationMapper {
    fun mapConversation(
        id: Long,
        name: String,
        owner: String,
        participants: List<String>,
        lastMessage: String?,
        lastReceived: Long
    ) = Conversation(id, name, owner, participants, lastMessage, lastReceived)
}

private object ContactMapper {
    fun mapContact(
        id: String,
        name: String,
        acceptedOnce: Boolean,
    ) = Contact(id, name, acceptedOnce)
}