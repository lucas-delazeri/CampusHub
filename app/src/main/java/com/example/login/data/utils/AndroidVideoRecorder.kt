package com.example.login.data.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AndroidVideoRecorder(private val context: Context) {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCameraSession(lifecycleOwner: LifecycleOwner, onCaptureReady: (VideoCapture<Recorder>) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    videoCapture
                )

                videoCapture?.let { onCaptureReady(it) }

            } catch (exc: Exception) {
                Log.e("AndroidVideoRecorder", "Falha ao vincular casos de uso da câmera", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording(
        onVideoSaved: (uriPath: String) -> Unit,
        onError: (exception: Throwable) -> Unit
    ) {
        if (currentRecording != null) return
        val videoCapture = this.videoCapture ?: run {
            onError(IllegalStateException("Câmera não inicializada ou sessão não iniciada."))
            return
        }

        val name = "Video_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Videos")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        currentRecording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d("AndroidVideoRecorder", "Gravação iniciada.")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = recordEvent.outputResults.outputUri.toString()
                            Log.d("AndroidVideoRecorder", "Gravação finalizada com sucesso: $msg")
                            onVideoSaved(msg)
                        } else {
                            currentRecording?.close()
                            currentRecording = null
                            onError(RuntimeIOException("Erro ao gravar vídeo: ${recordEvent.error}"))
                        }
                        currentRecording = null
                    }
                }
            }
    }

    fun stopRecording() {
        currentRecording?.let {
            it.stop()
            currentRecording = null
        }
    }

    fun release() {
        cameraExecutor.shutdown()
    }

    class RuntimeIOException(message: String) : Exception(message)
}