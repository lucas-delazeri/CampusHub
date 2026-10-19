package com.example.login.util
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

import kotlin.Unit;

class AndroidAudioPlayer(
    private val context: Context
) {
    private var player: MediaPlayer? = null

    fun playFile(uri: Uri, onCompletion: () -> Unit = {}) {
        player?.stop()
        player?.release()

        player = MediaPlayer.create(context, uri)?.apply {
            setOnCompletionListener {
                onCompletion()
            }
            start()
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}