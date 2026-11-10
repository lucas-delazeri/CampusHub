package com.example.login.data.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class QrCodeUtilsTest {

    @Test
    fun getCheckInPayload_formatsCorrectly() {
        val payload = QrCodeUtils.getCheckInPayload(5, "user_xyz")
        assertEquals("CAMPUSHUB_CHECKIN:5:user_xyz", payload)
    }

    @Test
    fun parseCheckInPayload_validPayload_parsesEventIdAndUserId() {
        val result = QrCodeUtils.parseCheckInPayload("CAMPUSHUB_CHECKIN:12:student_abc")
        assertNotNull(result)
        assertEquals(12, result?.first)
        assertEquals("student_abc", result?.second)
    }

    @Test
    fun parseCheckInPayload_invalidPayload_returnsNull() {
        assertNull(QrCodeUtils.parseCheckInPayload("INVALID_PAYLOAD"))
        assertNull(QrCodeUtils.parseCheckInPayload("CAMPUSHUB_CHECKIN:abc:user"))
    }
}
