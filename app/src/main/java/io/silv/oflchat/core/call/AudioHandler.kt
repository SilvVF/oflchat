package io.silv.oflchat.core.call

interface AudioHandler {
    /**
     * Called when a room is started.
     */
    fun start()

    /**
     * Called when a room is disconnected.
     */
    fun stop()
}