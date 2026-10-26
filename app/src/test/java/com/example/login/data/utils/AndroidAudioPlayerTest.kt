package com.example.login.data.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.login.util.AndroidAudioPlayer
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class AndroidAudioPlayerTest {

    private lateinit var context: Context
    private lateinit var audioPlayer: AndroidAudioPlayer

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        audioPlayer = AndroidAudioPlayer(context)
    }

    @Test
    fun stop_whenPlayerNull_doesNotCrash() {
        audioPlayer.stop()
    }

    @Test
    fun stop_whenPlayerActive_callsStopAndRelease() {
        val mockMediaPlayer = mockk<MediaPlayer>(relaxed = true)
        val field: Field = audioPlayer.javaClass.getDeclaredField("player")
        field.isAccessible = true
        field.set(audioPlayer, mockMediaPlayer)

        audioPlayer.stop()

        verify { mockMediaPlayer.stop() }
        verify { mockMediaPlayer.release() }
    }
}
