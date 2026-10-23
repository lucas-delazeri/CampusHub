package com.example.login.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class EventRepositoryTest {

    companion object {
        private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        private val mockDbRef = mockk<DatabaseReference>(relaxed = true)

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            mockkStatic(FirebaseAuth::class)
            mockkStatic(FirebaseDatabase::class)

            every { FirebaseAuth.getInstance() } returns mockAuth
            val mockDb = mockk<FirebaseDatabase>(relaxed = true)
            every { FirebaseDatabase.getInstance() } returns mockDb
            every { mockDb.reference } returns mockDbRef
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            unmockkAll()
        }
    }

    @Before
    fun setUp() {
        every { mockAuth.currentUser } returns null
        EventRepository.clearLocalData()
    }

    @Test
    fun initialEventsList_containsDefaultItems() {
        val events = EventRepository.allEvents
        Assert.assertEquals(5, events.size)
        Assert.assertEquals("II Feira de Profissões e Carreiras", events[0].title)
    }

    @Test
    fun enrollment_togglesCorrectly() {
        Assert.assertFalse(EventRepository.isEnrolled(1))

        EventRepository.enroll(1)
        Assert.assertTrue(EventRepository.isEnrolled(1))

        EventRepository.unenroll(1)
        Assert.assertFalse(EventRepository.isEnrolled(1))
    }

    @Test
    fun favorites_togglesCorrectly() {
        Assert.assertFalse(EventRepository.isFavorite(10))

        EventRepository.favorited(10)
        Assert.assertTrue(EventRepository.isFavorite(10))

        EventRepository.unfavorite(10)
        Assert.assertFalse(EventRepository.isFavorite(10))
    }

    @Test
    fun clearLocalData_resetsEnrollmentsAndFavorites() {
        EventRepository.enroll(1)
        EventRepository.favorited(2)

        EventRepository.clearLocalData()

        Assert.assertFalse(EventRepository.isEnrolled(1))
        Assert.assertFalse(EventRepository.isFavorite(2))
    }

    @Test
    fun addComment_whenUserNotAuthenticated_returnsError() {
        every { mockAuth.currentUser } returns null
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.addComment(1, "Ótimo!", 5, null, null, null) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("Usuário não autenticado", messageResult)
    }

    @Test
    fun addComment_whenContentAndMediaEmpty_returnsError() {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns user
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.addComment(1, "   ", null, null, null, null) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("O comentário não pode ser vazio", messageResult)
    }

    @Test
    fun editComment_whenUserNotAuthenticated_returnsError() {
        every { mockAuth.currentUser } returns null
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.editComment(1, "c1", "novo", 4, null) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("Usuário não autenticado", messageResult)
    }

    @Test
    fun editComment_whenContentEmpty_returnsError() {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns user
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.editComment(1, "c1", "", 4, null) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("O comentário não pode ser vazio", messageResult)
    }

    @Test
    fun deleteComment_whenUserNotAuthenticated_returnsError() {
        every { mockAuth.currentUser } returns null
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.deleteComment(1, "c1") { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("Usuário não autenticado", messageResult)
    }

    @Test
    fun rateEvent_whenUserNotAuthenticated_returnsError() {
        every { mockAuth.currentUser } returns null
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.rateEvent(1, 5) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("Usuário não autenticado", messageResult)
    }

    @Test
    fun rateEvent_whenRatingInvalid_returnsError() {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns user
        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.rateEvent(1, 6) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(false, successResult)
        Assert.assertEquals("Nota inválida", messageResult)
    }
}
