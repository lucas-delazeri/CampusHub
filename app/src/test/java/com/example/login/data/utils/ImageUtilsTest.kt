package com.example.login.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ImageUtilsTest {

    private lateinit var context: Context
    private lateinit var mockUri: Uri

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        mockUri = mockk(relaxed = true)

        mockkStatic(BitmapFactory::class)
        mockkStatic(Bitmap::class)
        mockkStatic(Base64::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(BitmapFactory::class)
        unmockkStatic(Bitmap::class)
        unmockkStatic(Base64::class)
    }

    @Test
    fun uriToBase64_whenInputStreamFails_returnsNull() {
        every { context.contentResolver.openInputStream(mockUri) } throws RuntimeException("Erro ao abrir imagem")

        val result = ImageUtils.uriToBase64(context, mockUri)

        assertNull(result)
    }

    @Test
    fun uriToBase64_whenResolverReturnsNull_returnsNull() {
        every { context.contentResolver.openInputStream(mockUri) } returns null

        val result = ImageUtils.uriToBase64(context, mockUri)

        assertNull(result)
    }

    @Test
    fun uriToBase64_whenImageIsLargerThanMaxSize_scalesBitmapAndReturnsBase64() {
        every { context.contentResolver.openInputStream(mockUri) } answers {
            ByteArrayInputStream(byteArrayOf(1, 2, 3))
        }

        val originalBitmap = mockk<Bitmap>(relaxed = true)
        every { originalBitmap.width } returns 1600
        every { originalBitmap.height } returns 1200

        val scaledBitmap = mockk<Bitmap>(relaxed = true)

        every { BitmapFactory.decodeStream(any(), any(), any()) } answers {
            val opts = arg<BitmapFactory.Options?>(2)
            if (opts?.inJustDecodeBounds == true) {
                opts.outWidth = 1600
                opts.outHeight = 1200
                null
            } else {
                originalBitmap
            }
        }

        every {
            Bitmap.createScaledBitmap(originalBitmap, 800, 600, true)
        } returns scaledBitmap

        every { Base64.encodeToString(any(), Base64.NO_WRAP) } returns "base64StringScaled"

        val result = ImageUtils.uriToBase64(context, mockUri, maxSize = 800, quality = 70)

        assertEquals("base64StringScaled", result)
        verify { Bitmap.createScaledBitmap(originalBitmap, 800, 600, true) }
        verify { scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, any()) }
    }

    @Test
    fun uriToBase64_whenImageIsSmallerThanMaxSize_doesNotScaleBitmapAndReturnsBase64() {
        every { context.contentResolver.openInputStream(mockUri) } answers {
            ByteArrayInputStream(byteArrayOf(1, 2, 3))
        }

        val originalBitmap = mockk<Bitmap>(relaxed = true)
        every { originalBitmap.width } returns 400
        every { originalBitmap.height } returns 300

        every { BitmapFactory.decodeStream(any(), any(), any()) } answers {
            val opts = arg<BitmapFactory.Options?>(2)
            if (opts?.inJustDecodeBounds == true) {
                opts.outWidth = 400
                opts.outHeight = 300
                null
            } else {
                originalBitmap
            }
        }

        every { Base64.encodeToString(any(), Base64.NO_WRAP) } returns "base64StringUnscaled"

        val result = ImageUtils.uriToBase64(context, mockUri, maxSize = 800, quality = 70)

        assertEquals("base64StringUnscaled", result)
        verify(exactly = 0) { Bitmap.createScaledBitmap(any(), any(), any(), any()) }
        verify { originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, any()) }
    }
}
