package com.example.login.data.utils

import android.content.Context
import android.media.MediaRecorder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field

class AndroidAudioRecorderTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val mediaRecorder = mockk<MediaRecorder>(relaxed = true)
    private lateinit var context: Context
    private lateinit var recorder: AndroidAudioRecorder

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        recorder = AndroidAudioRecorder(context)
    }

    @Test
    fun stop_whenRecorderNull_doesNotCrash() {
        recorder.stop()
    }

    @Test
    fun stop_whenRecorderActive_callsStopAndReset() {
        val mockMediaRecorder = mockk<MediaRecorder>(relaxed = true)
        val field: Field = recorder.javaClass.getDeclaredField("recorder")
        field.isAccessible = true
        field.set(recorder, mockMediaRecorder)

        recorder.stop()

        verify { mockMediaRecorder.stop() }
        verify { mockMediaRecorder.reset() }
    }

    @Test
    fun start_sets_recorder_on_correct_order() {
        val sut = AndroidAudioRecorder(context, sdkInt = 33) { _, _ -> mediaRecorder }
        val file = tmp.newFile("audio.m4a")

        sut.start(file)

        verifyOrder {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(any<FileDescriptor>())
            mediaRecorder.prepare()
            mediaRecorder.start()
        }
    }

    @Test
    fun `if prepare fails, start is not called`() {
        every { mediaRecorder.prepare() } throws IOException("falha")
        val sut = AndroidAudioRecorder(context, recorderFactory = { _, _ -> mediaRecorder })

        assertThrows(IOException::class.java) { sut.start(tmp.newFile()) }

        verify(exactly = 0) { mediaRecorder.start() }
    }
}
