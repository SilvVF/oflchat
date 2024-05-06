package io.silv.oflchat.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioSource
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import io.silv.oflchat.applicationContext
import io.silv.oflchat.ui.EventProducer
import timber.log.Timber
import java.io.Closeable
import java.io.File
import kotlin.time.Duration.Companion.seconds

object MediaHelper {

    private val dir by lazy { File(applicationContext.cacheDir, "tmp") }

    private fun createOutputFile(): File {

        dir.mkdirs()

        return File.createTempFile(
            dir.nameWithoutExtension.padEnd(3), // Prefix must be 3+ chars
            null,
            dir,
        )
            .also { it.deleteOnExit() }
    }

    fun create(context: Context): ClosableRecorder? {
        return when {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ->
                runCatching {

                    val outputFile = createOutputFile()

                    ClosableRecorder(context, outputFile).apply {
                        setAudioSource(AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        setMaxDuration(30.seconds.inWholeMilliseconds.toInt())
                        setOutputFile(outputFile)
                    }
                }
                    .onFailure { Timber.d(it) }
                    .getOrNull()
           ActivityCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.RECORD_AUDIO
           ) == PackageManager.PERMISSION_GRANTED -> {
               null
           }
           else -> null
        }
    }

    @Stable
    @RequiresApi(Build.VERSION_CODES.S)
    class ClosableRecorder(context: Context, val outputFile: File) : MediaRecorder(context), Closeable {

        override fun close() {
            stop()
            release()
            outputFile.delete()
        }
    }

    @Stable
    sealed interface RecorderEvent {
        @Stable sealed interface Error: RecorderEvent {
            @Stable data object GenericError: Error
            @Stable data class InitializationError(val throwable: Throwable): Error
            @Stable data class RecordingError(val throwable: Throwable): Error
        }
        @Stable data class AudioCreated(val file: File): RecorderEvent
    }

    class RecorderState: EventProducer<RecorderEvent> by EventProducer.default() {

        private var recorder: MediaHelper.ClosableRecorder? = null

        var recording by mutableStateOf(false)

        val canInitialize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        fun start() {
            with(MediaHelper.create(applicationContext)) {
                if (this == null) {
                    tryEmitEvent(RecorderEvent.Error.GenericError)
                    return
                }
                recorder = this
                runCatching { prepare() }
                    .onFailure {
                        dispose()
                        tryEmitEvent(RecorderEvent.Error.RecordingError(it))
                    }
                    .onSuccess {
                        start()
                        recording = true
                    }
            }
        }

        fun stop() {
            recorder?.let {
                tryEmitEvent(RecorderEvent.AudioCreated(it.outputFile))
                it.stop()
                it.release()
            }
            recorder = null
            recording = false
        }

        fun dispose() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorder?.close()
                recording = false
            }
        }
    }
}
