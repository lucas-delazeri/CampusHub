package com.example.login.data.repository

import com.example.login.data.model.Event
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
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
    fun createEvent_addsToAllEventsAndDatabase() {
        val newEvent = Event(id = 100, title = "Novo Evento")
        val childRef = mockk<DatabaseReference>(relaxed = true)
        every { mockDbRef.child("events").child("100") } returns childRef

        EventRepository.createEvent(newEvent)

        Assert.assertTrue(EventRepository.allEvents.any { it.id == 100 })
        verify { childRef.setValue(newEvent) }
    }

    @Test
    fun loadUserRole_whenUserAuthenticated_queriesDatabase() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "u123"
        every { mockAuth.currentUser } returns user

        val task = mockk<Task<DataSnapshot>>(relaxed = true)
        val roleRef = mockk<DatabaseReference>(relaxed = true)
        every { mockDbRef.child("users").child("u123").child("role") } returns roleRef
        every { roleRef.get() } returns task

        EventRepository.loadUserRole()

        verify { roleRef.get() }
    }

    @Test
    fun loadUserEnrollments_whenUserAuthenticated_queriesDatabase() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "u123"
        every { mockAuth.currentUser } returns user

        val task = mockk<Task<DataSnapshot>>(relaxed = true)
        val enrollRef = mockk<DatabaseReference>(relaxed = true)
        every { mockDbRef.child("users").child("u123").child("enrolledEvents") } returns enrollRef
        every { enrollRef.get() } returns task

        EventRepository.loadUserEnrollments()

        verify { enrollRef.get() }
    }

    @Test
    fun loadUserFavoriteEvents_whenUserAuthenticated_queriesDatabase() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "u123"
        every { mockAuth.currentUser } returns user

        val task = mockk<Task<DataSnapshot>>(relaxed = true)
        val favRef = mockk<DatabaseReference>(relaxed = true)
        every { mockDbRef.child("users").child("u123").child("favoriteEvents") } returns favRef
        every { favRef.get() } returns task

        EventRepository.loadUserFavoriteEvents()

        verify { favRef.get() }
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
    fun addComment_whenUserAuthenticated_savesComment() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "u1"
        every { user.displayName } returns "Maria"
        every { user.email } returns "maria@example.com"
        every { mockAuth.currentUser } returns user

        val commentsRef = mockk<DatabaseReference>(relaxed = true)
        val pushRef = mockk<DatabaseReference>(relaxed = true)
        val commentItemRef = mockk<DatabaseReference>(relaxed = true)
        val voidTask = mockk<Task<Void>>(relaxed = true)

        every { mockDbRef.child("events").child("1").child("comments") } returns commentsRef
        every { commentsRef.push() } returns pushRef
        every { pushRef.key } returns "comment123"
        every { commentsRef.child("comment123") } returns commentItemRef
        every { commentItemRef.setValue(any()) } returns voidTask

        val slotSuccess = slot<OnSuccessListener<in Void>>()
        every { voidTask.addOnSuccessListener(capture(slotSuccess)) } answers {
            slotSuccess.captured.onSuccess(null)
            voidTask
        }

        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.addComment(1, "Excelente evento", 5, null, null, null) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(true, successResult)
        Assert.assertEquals("Comentário adicionado!", messageResult)
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
    fun editComment_whenValid_updatesCommentInDatabase() {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns user

        val commentRef = mockk<DatabaseReference>(relaxed = true)
        val voidTask = mockk<Task<Void>>(relaxed = true)

        every { mockDbRef.child("events").child("1").child("comments").child("c1") } returns commentRef
        every { commentRef.updateChildren(any()) } returns voidTask

        val slotSuccess = slot<OnSuccessListener<in Void>>()
        every { voidTask.addOnSuccessListener(capture(slotSuccess)) } answers {
            slotSuccess.captured.onSuccess(null)
            voidTask
        }

        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.editComment(1, "c1", "Comentário Editado", 4, "audio.mp3") { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(true, successResult)
        Assert.assertEquals("Comentário editado!", messageResult)
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
    fun deleteComment_whenUserAuthenticated_removesComment() {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns user

        val commentRef = mockk<DatabaseReference>(relaxed = true)
        val voidTask = mockk<Task<Void>>(relaxed = true)

        every { mockDbRef.child("events").child("1").child("comments").child("c1") } returns commentRef
        every { commentRef.removeValue() } returns voidTask

        val slotSuccess = slot<OnSuccessListener<in Void>>()
        every { voidTask.addOnSuccessListener(capture(slotSuccess)) } answers {
            slotSuccess.captured.onSuccess(null)
            voidTask
        }

        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.deleteComment(1, "c1") { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(true, successResult)
        Assert.assertEquals("Comentário excluído!", messageResult)
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

    @Test
    fun rateEvent_whenValid_savesRating() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "u1"
        every { mockAuth.currentUser } returns user

        val ratingRef = mockk<DatabaseReference>(relaxed = true)
        val voidTask = mockk<Task<Void>>(relaxed = true)

        every { mockDbRef.child("events").child("1").child("ratings").child("u1") } returns ratingRef
        every { ratingRef.setValue(5) } returns voidTask

        val slotSuccess = slot<OnSuccessListener<in Void>>()
        every { voidTask.addOnSuccessListener(capture(slotSuccess)) } answers {
            slotSuccess.captured.onSuccess(null)
            voidTask
        }

        var successResult: Boolean? = null
        var messageResult: String? = null

        EventRepository.rateEvent(1, 5) { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertEquals(true, successResult)
        Assert.assertEquals("Avaliação salva com sucesso!", messageResult)
    }

    @Test
    fun listenToCommentsAndRemove_managesEventListener() {
        val commentsRef = mockk<DatabaseReference>(relaxed = true)
        every { mockDbRef.child("events").child("1").child("comments") } returns commentsRef

        val listener = EventRepository.listenToComments(1) { }

        verify { commentsRef.addValueEventListener(listener) }

        EventRepository.removeCommentsListener(1, listener)
        verify { commentsRef.removeEventListener(listener) }
    }

    @Test
    fun listenToRatingsAndRemove_managesEventListener() {
        val ratingsRef = mockk<DatabaseReference>(relaxed = true)
        every { mockDbRef.child("events").child("1").child("ratings") } returns ratingsRef

        val listener = EventRepository.listenToRatings(1) { _, _, _ -> }

        verify { ratingsRef.addValueEventListener(listener) }

        EventRepository.removeRatingsListener(1, listener)
        verify { ratingsRef.removeEventListener(listener) }
    }
}
