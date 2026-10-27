package com.example.login.data.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.login.util.AndroidAudioPlayer
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AndroidAudioPlayerTest {

    private lateinit var context: Context
    private lateinit var uri: Uri
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioPlayer: AndroidAudioPlayer

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        uri = mockk()
        mediaPlayer = mockk(relaxed = true)

        mockkStatic(MediaPlayer::class)
        every { MediaPlayer.create(context, uri) } returns mediaPlayer

        audioPlayer = AndroidAudioPlayer(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun playFile_createsPlayerAndStarts() {
        audioPlayer.playFile(uri)

        verify { MediaPlayer.create(context, uri) }
        verify { mediaPlayer.start() }
    }

    @Test
    fun playFile_onCompletion_invokesCallback() {
        val listener = slot<MediaPlayer.OnCompletionListener>()
        every { mediaPlayer.setOnCompletionListener(capture(listener)) } just Runs
        var called = false

        audioPlayer.playFile(uri) { called = true }
        listener.captured.onCompletion(mediaPlayer)

        assertTrue(called)
    }

    @Test
    fun playFile_whenAlreadyPlaying_stopsAndReleasesPrevious() {
        val first = mockk<MediaPlayer>(relaxed = true)
        val second = mockk<MediaPlayer>(relaxed = true)
        every { MediaPlayer.create(context, uri) } returnsMany listOf(first, second)

        audioPlayer.playFile(uri)
        audioPlayer.playFile(uri)

        verify { first.stop() }
        verify { first.release() }
        verify { second.start() }
    }

    @Test
    fun playFile_whenCreateReturnsNull_doesNotCrash() {
        every { MediaPlayer.create(context, uri) } returns null

        audioPlayer.playFile(uri)
        audioPlayer.stop()
    }

    @Test
    fun stop_whenPlayerNull_doesNotCrash() {
        audioPlayer.stop()
    }

    @Test
    fun stop_afterPlay_stopsAndReleases() {
        audioPlayer.playFile(uri)

        audioPlayer.stop()

        verify { mediaPlayer.stop() }
        verify { mediaPlayer.release() }
    }

    @Test
    fun stop_calledTwice_releasesOnlyOnce() {
        audioPlayer.playFile(uri)

        audioPlayer.stop()
        audioPlayer.stop()

        verify(exactly = 1) { mediaPlayer.release() }
    }
}
