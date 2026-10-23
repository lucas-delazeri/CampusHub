package com.example.login.data.model

import org.junit.Assert
import org.junit.Test

class EventTest {

    @Test
    fun defaultConstructor_setsExpectedDefaults() {
        val event = Event()

        Assert.assertEquals(0, event.id)
        Assert.assertEquals("", event.title)
        Assert.assertEquals("", event.description)
        Assert.assertEquals("", event.date)
        Assert.assertEquals("", event.location)
        Assert.assertEquals("", event.organizer)
        Assert.assertEquals("", event.category)
        Assert.assertFalse(event.isEnded)
        Assert.assertNull(event.latitude)
        Assert.assertNull(event.longitude)
    }

    @Test
    fun customConstructor_storesAllProperties() {
        val event = Event(
            id = 10,
            title = "Feira",
            description = "Desc",
            date = "20/10/2026",
            location = "Auditório",
            organizer = "DA",
            category = "Geral",
            isEnded = true,
            latitude = -23.55,
            longitude = -46.63
        )

        Assert.assertEquals(10, event.id)
        Assert.assertEquals("Feira", event.title)
        Assert.assertEquals("Desc", event.description)
        Assert.assertEquals("20/10/2026", event.date)
        Assert.assertEquals("Auditório", event.location)
        Assert.assertEquals("DA", event.organizer)
        Assert.assertEquals("Geral", event.category)
        Assert.assertTrue(event.isEnded)
        Assert.assertEquals(-23.55, event.latitude!!, 0.0001)
        Assert.assertEquals(-46.63, event.longitude!!, 0.0001)
    }
}
