package com.example.login.data.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtils {
    fun generateQrCodeBitmap(content: String, size: Int = 512): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    fun getCheckInPayload(eventId: Int, userId: String): String {
        return "CAMPUSHUB_CHECKIN:$eventId:$userId"
    }

    fun parseCheckInPayload(payload: String): Pair<Int, String>? {
        if (!payload.startsWith("CAMPUSHUB_CHECKIN:")) return null
        val parts = payload.removePrefix("CAMPUSHUB_CHECKIN:").split(":")
        if (parts.size < 2) return null
        val eventId = parts[0].toIntOrNull() ?: return null
        val userId = parts.subList(1, parts.size).joinToString(":")
        return Pair(eventId, userId)
    }
}
