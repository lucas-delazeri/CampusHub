package com.example.login

import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.example.login.ui.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var auth: FirebaseAuth

    private fun sampleEvent(id: Int = 1, isEnded: Boolean = false) = Event(
        id = id,
        title = "title",
        description = "description",
        date = "02/02/2000",
        location = "location",
        organizer = "organizer",
        category = "category",
        isEnded = isEnded,
        latitude = 48.8566,
        longitude = 2.3522
    )

    @Before
    fun setUp() {
        auth = mockk<FirebaseAuth>(relaxed = true)
        every { auth.currentUser } returns null
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns auth

        mockkObject(EventRepository)
        every { EventRepository.loadUserEnrollments() } just Runs
        every { EventRepository.loadUserFavoriteEvents() } just Runs
        every { EventRepository.loadUserRole() } just Runs
        every { EventRepository.userRole } returns "professor"
        every { EventRepository.allEvents } returns listOf(sampleEvent(id = 5))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createEvent_nonTeacher_isRejected() {
        every { EventRepository.userRole } returns "aluno"
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.createEvent("t", "d", "date", "loc", "cat") { message = it }

        Assert.assertEquals("Apenas professores podem criar eventos.", message)
        verify(exactly = 0) { EventRepository.createEvent(any()) }
    }

    @Test
    fun createEvent_blankTitle_isRejected() {
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.createEvent("", "d", "date", "loc", "cat") { message = it }

        Assert.assertEquals("Preencha todos os campos obrigatórios.", message)
        verify(exactly = 0) { EventRepository.createEvent(any()) }
    }

    @Test
    fun createEvent_teacher_createsEventWithDefaults() {
        val user = mockk<FirebaseUser>()
        every { user.displayName } returns "Professor"
        every { user.email } returns "silva@escola.com"
        every { auth.currentUser } returns user

        val captured = slot<Event>()
        every { EventRepository.createEvent(capture(captured)) } just Runs

        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.createEvent("Aula", "Desc", "02/02/2000", "Sala 1", "Tecnologia") { message = it }

        Assert.assertEquals("Evento criado com sucesso!", message)
        Assert.assertEquals(6, captured.captured.id)
        Assert.assertEquals("Tecnologia", captured.captured.category)
        Assert.assertEquals("Professor", captured.captured.organizer)
        Assert.assertEquals(-23.550520, captured.captured.latitude)
        Assert.assertEquals(-46.633308, captured.captured.longitude)
        Assert.assertFalse(captured.captured.isEnded)
    }

    @Test
    fun toggleEnrollment_endedEvent_isBlocked() {
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.toggleEnrollment(sampleEvent(isEnded = true)) { message = it }

        Assert.assertEquals(
            "Este evento já encerrou. Ações de inscrição estão desativadas.",
            message
        )
        verify(exactly = 0) { EventRepository.enroll(any()) }
    }

    @Test
    fun toggleEnrollment_notEnrolled_enrolls() {
        every { EventRepository.isEnrolled(1) } returns false
        every { EventRepository.enroll(1) } just Runs
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.toggleEnrollment(sampleEvent()) { message = it }

        Assert.assertEquals("Inscrição realizada com sucesso!", message)
        verify { EventRepository.enroll(1) }
    }

    @Test
    fun isTeacher_isCaseInsensitive() {
        every { EventRepository.userRole } returns "PROFESSOR"
        viewModel = HomeViewModel()

        Assert.assertTrue(viewModel.isTeacher)
    }
}