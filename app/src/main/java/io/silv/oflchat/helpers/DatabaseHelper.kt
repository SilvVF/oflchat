package io.silv.oflchat.helpers

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.silv.Connection
import io.silv.Conversation
import io.silv.Database
import io.silv.Member
import io.silv.oflchat.applicationContext
import io.silv.oflchat.core.AndroidDatabaseHandler
import io.silv.oflchat.core.DatabaseHandler
import io.silv.oflchat.core.database.ConnectionDao
import io.silv.oflchat.core.model.MemberEntity
import kotlinx.datetime.Instant

object DatabaseHelper {

    private val StringListAdapter  = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            if (databaseValue.isEmpty()) {
                listOf()
            } else {
                databaseValue.split(",")
            }
        override fun encode(value: List<String>) = value.joinToString(separator = ",")
    }

    internal object MemberRoleAdapter : ColumnAdapter<MemberEntity.Role, String> {
        override fun decode(databaseValue: String): MemberEntity.Role = when (databaseValue) {
            ADMIN -> MemberEntity.Role.Admin
            MEMBER -> MemberEntity.Role.Member
            else -> MemberEntity.Role.Unknown(databaseValue)
        }

        override fun encode(value: MemberEntity.Role): String = when (value) {
            MemberEntity.Role.Admin -> ADMIN
            MemberEntity.Role.Member -> MEMBER
            is MemberEntity.Role.Unknown -> value.name
        }

        private const val ADMIN = "wire_admin"
        private const val MEMBER = "wire_member"
    }

    internal val InstantAdapter = object : ColumnAdapter<Instant, Long> {
        override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)

        override fun encode(value: Instant): Long = value.toEpochMilliseconds()
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
            context = applicationContext,
            name = DB_NAME
        )
        AndroidDatabaseHandler(
            db = Database(
                driver = driver,
                ConnectionAdapter = Connection.Adapter(
                    statusAdapter = EnumColumnAdapter(),
                    last_update_dateAdapter = InstantAdapter
                ),
                ConversationAdapter = Conversation.Adapter(
                    typeAdapter = EnumColumnAdapter(),
                    muted_statusAdapter = EnumColumnAdapter()
                ),
                MemberAdapter = Member.Adapter(
                    roleAdapter = MemberRoleAdapter
                )
            ),
            driver = driver
        )
    }

    fun connectionDao(): ConnectionDao = ConnectionDao(handler)
}