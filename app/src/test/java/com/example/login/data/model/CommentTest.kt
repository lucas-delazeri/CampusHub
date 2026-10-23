package com.example.login.data.model

import org.junit.Assert
import org.junit.Test

class CommentTest {

    @Test
    fun defaultConstructor_setsExpectedDefaults() {
        val comment = Comment()

        Assert.assertEquals("", comment.id)
        Assert.assertEquals(0, comment.eventId)
        Assert.assertEquals("", comment.authorId)
        Assert.assertEquals("", comment.authorName)
        Assert.assertEquals("", comment.content)
        Assert.assertEquals("", comment.publishedAt)
        Assert.assertNull(comment.rating)
        Assert.assertNull(comment.audioUri)
        Assert.assertNull(comment.imageBase64)
        Assert.assertNull(comment.videoUrl)
    }

    @Test
    fun customConstructor_storesAllProperties() {
        val comment = Comment(
            id = "c1",
            eventId = 5,
            authorId = "u1",
            authorName = "Ana",
            content = "Ótimo evento!",
            publishedAt = "23/10/2026 10:00",
            rating = 5,
            audioUri = "audio.mp3",
            imageBase64 = "imgData",
            videoUrl = "video.mp4"
        )

        Assert.assertEquals("c1", comment.id)
        Assert.assertEquals(5, comment.eventId)
        Assert.assertEquals("u1", comment.authorId)
        Assert.assertEquals("Ana", comment.authorName)
        Assert.assertEquals("Ótimo evento!", comment.content)
        Assert.assertEquals("23/10/2026 10:00", comment.publishedAt)
        Assert.assertEquals(Integer.valueOf(5), comment.rating)
        Assert.assertEquals("audio.mp3", comment.audioUri)
        Assert.assertEquals("imgData", comment.imageBase64)
        Assert.assertEquals("video.mp4", comment.videoUrl)
    }
}
