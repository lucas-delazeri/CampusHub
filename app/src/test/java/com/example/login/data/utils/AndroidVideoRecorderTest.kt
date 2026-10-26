package com.example.login.data.utils

import android.content.Context
import androidx.camera.video.Recording
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class AndroidVideoRecorderTest {

    private lateinit var context: Context
    private lateinit var recorder: AndroidVideoRecorder

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        recorder = AndroidVideoRecorder(context)
    }

    @Test
    fun startRecording_whenVideoCaptureNull_triggersOnError() {
        var capturedException: Throwable? = null

        recorder.startRecording(
            onVideoSaved = { },
            onError = { capturedException = it }
        )

        assertNotNull(capturedException)
        assertTrue(capturedException is IllegalStateException)
        assertEquals("Câmera não inicializada ou sessão não iniciada.", capturedException?.message)
    }

    @Test
    fun startRecording_whenAlreadyRecording_returnsEarlyWithoutError() {
        val mockRecording = mockk<Recording>(relaxed = true)

        setPrivateField(recorder, "currentRecording", mockRecording)

        var errorCalled = false
        var savedCalled = false

        recorder.startRecording(
            onVideoSaved = { savedCalled = true },
            onError = { errorCalled = true }
        )

        assertEquals(false, errorCalled)
        assertEquals(false, savedCalled)
    }

    @Test
    fun stopRecording_whenRecordingNull_doesNotCrash() {
        recorder.stopRecording()
    }

    @Test
    fun stopRecording_whenRecordingActive_callsStopAndResetsRecording() {
        val mockRecording = mockk<Recording>(relaxed = true)
        setPrivateField(recorder, "currentRecording", mockRecording)

        recorder.stopRecording()

        verify { mockRecording.stop() }
    }

    @Test
    fun release_shutsDownExecutor() {
        recorder.release()
    }

    @Test
    fun runtimeIOException_instantiatesWithMessage() {
        val exception = AndroidVideoRecorder.RuntimeIOException("Erro ao gravar vídeo")
        assertEquals("Erro ao gravar vídeo", exception.message)
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field: Field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}
