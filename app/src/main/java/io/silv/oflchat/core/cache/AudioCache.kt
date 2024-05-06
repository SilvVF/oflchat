package io.silv.oflchat.core.cache

import io.silv.oflchat.applicationContext
import kotlinx.coroutines.channels.Channel
import okio.IOException
import java.io.File
import java.io.InputStream

/**
 * Class used to create cover cache.
 * It is used to store the covers of the library.
 * Names of files are created with the md5 of the thumbnail URL.
 *
 * @param context the application context.
 * @constructor creates an instance of the cover cache.
 */
object AudioCache {

    val changes: Channel<Unit> = Channel()

    private const val CACHE_DIR = "audio"

    /**
     * Cache directory used for cache management.
     */
    private val cacheDir by lazy { getCacheDir(CACHE_DIR) }

    /**
     * relative path of the audio file hashes values using MD5
     * @return conversationId/contactId/messageId
     */
    private fun audioFileHash(conversationId: String, contactId: String, messageId: String): String = buildString {
        append(DiskUtil.hashKeyForDisk(conversationId))
        append(File.separator)
        append(DiskUtil.hashKeyForDisk(contactId))
        append(File.separator)
        append(DiskUtil.hashKeyForDisk(messageId))
    }

    /**
     * relative path of the audio file hashes values using MD5
     * @return conversationId/contactId
     */
    private fun audioFileHash(conversationId: String, contactId: String): String = buildString {
        append(DiskUtil.hashKeyForDisk(conversationId))
        append(File.separator)
        append(DiskUtil.hashKeyForDisk(contactId))
        append(File.separator)
    }

    /**
     * relative path of the audio file hashes values using MD5
     * @return conversationId
     */
    private fun audioFileHash(conversationId: String): String = buildString {
        append(DiskUtil.hashKeyForDisk(conversationId))
    }

    /**
     * Returns the audio file from cache.
     *
     * @param contactId contact that created the audio file.
     * @return audio file.
     */
    fun getAudioFile(conversationId: String, contactId: String, messageId: String): File {
        return File(cacheDir, audioFileHash(conversationId, contactId, messageId))
    }

    /**
     * Returns the audio files from cache for a conversation contact.
     *
     * @param contactId contact that created the audio file.
     * @return audio file.
     */
    fun getContactDirectory(conversationId: String, contactId: String): File {
        return File(cacheDir, audioFileHash(conversationId, contactId))
    }

    /**
     * Returns the audio files from cache for a conversation.
     *
     * @param contactId contact that created the audio file.
     * @return audio file.
     */
    fun getConversationDirectory(conversationId: String): File {
        return File(cacheDir, audioFileHash(conversationId))
    }

    @Throws(IOException::class)
    fun setAudioFileToCache(conversationId: String, contactId: String, messageId: String, inputStream: InputStream) {
        getAudioFile(conversationId, contactId, messageId).also { it.createNewFile() }.outputStream().use {
            inputStream.copyTo(it)
        }
        changes.trySend(Unit)
    }

    fun deleteFromCache(conversationId: String, contactId: String,  messageId: String): Int {
        var deleted = 0

        getAudioFile(conversationId, contactId, messageId).let {
            if (it.exists() && it.delete()) ++deleted
        }
        return deleted
    }

    fun deleteAllForContact(conversationId: String, contactId: String): Boolean {
        return File(cacheDir, audioFileHash(conversationId, contactId)).let {
            it.exists() && it.delete()
        }
    }

    fun deleteAllForConversation(conversationId: String): Boolean {
        return File(cacheDir, audioFileHash(conversationId)).let {
            it.exists() && it.delete()
        }
    }

    private fun getCacheDir(dir: String): File {
        return applicationContext.getExternalFilesDir(dir)
            ?: File(applicationContext.filesDir, dir).also { it.mkdirs() }
    }
}