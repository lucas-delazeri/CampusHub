package com.example.login.data.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context,
    private val sdkInt: Int = Build.VERSION.SDK_INT,
    private val recorderFactory: (Context, Int) -> MediaRecorder = { ctx, sdk ->
        if (sdk >= Build.VERSION_CODES.S) {
            @Suppress("NewApi")
            MediaRecorder(ctx)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }
) {
    private var recorder: MediaRecorder? = null
    private fun createRecorder(): MediaRecorder {
        return recorderFactory(context, sdkInt)
    }

    fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()
            recorder = this
        }
    }

    fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}