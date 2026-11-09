package com.example.login

import android.net.Uri
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
        every { EventRepository.listenToEvents() } just Runs
        every { EventRepository.userRole } returns "professor"
        every { EventRepository.allEvents } returns listOf(sampleEvent(id = 5))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun userProperties_fallbackValuesWhenUserNull() {
        viewModel = HomeViewModel()

        Assert.assertEquals("Estudante", viewModel.userName)
        Assert.assertEquals("No email", viewModel.email)
        Assert.assertNull(viewModel.currentUserId)
    }

    @Test
    fun userProperties_valuesWhenUserPresent() {
        val user = mockk<FirebaseUser>()
        every { user.displayName } returns "Lucas"
        every { user.email } returns "lucas@example.com"
        every { user.uid } returns "uid123"
        every { auth.currentUser } returns user

        viewModel = HomeViewModel()

        Assert.assertEquals("Lucas", viewModel.userName)
        Assert.assertEquals("lucas@example.com", viewModel.email)
        Assert.assertEquals("uid123", viewModel.currentUserId)
    }

    @Test
    fun changeTab_updatesCurrentTab() {
        viewModel = HomeViewModel()
        Assert.assertEquals(0, viewModel.currentTab)

        viewModel.changeTab(2)

        Assert.assertEquals(2, viewModel.currentTab)
    }

    @Test
    fun isTeacher_isCaseInsensitive() {
        every { EventRepository.userRole } returns "PROFESSOR"
        viewModel = HomeViewModel()

        Assert.assertTrue(viewModel.isTeacher)
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
    fun createEvent_blankCategory_usesDefaultCategory() {
        val captured = slot<Event>()
        every { EventRepository.createEvent(capture(captured)) } just Runs

        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.createEvent("Aula", "Desc", "02/02/2000", "Sala 1", "") { message = it }

        Assert.assertEquals("Evento criado com sucesso!", message)
        Assert.assertEquals("Geral", captured.captured.category)
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
        Assert.assertEquals(-23.550520, captured.captured.latitude!!, 0.0001)
        Assert.assertEquals(-46.633308, captured.captured.longitude!!, 0.0001)
        Assert.assertFalse(captured.captured.isEnded)
    }

    @Test
    fun isEventEnrolled_delegatesToRepository() {
        every { EventRepository.isEnrolled(10) } returns true
        viewModel = HomeViewModel()

        Assert.assertTrue(viewModel.isEventEnrolled(10))
        verify { EventRepository.isEnrolled(10) }
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
        every { EventRepository.enrollUser(1, any()) } returns "Inscrição realizada com sucesso!"
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.toggleEnrollment(sampleEvent(id = 1)) { message = it }

        Assert.assertEquals("Inscrição realizada com sucesso!", message)
        verify { EventRepository.enrollUser(1, any()) }
    }

    @Test
    fun toggleEnrollment_alreadyEnrolled_unenrolls() {
        every { EventRepository.isEnrolled(1) } returns true
        every { EventRepository.enrollUser(1, any()) } returns "Inscrição cancelada com sucesso!"
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.toggleEnrollment(sampleEvent(id = 1)) { message = it }

        Assert.assertEquals("Inscrição cancelada com sucesso!", message)
        verify { EventRepository.enrollUser(1, any()) }
    }

    @Test
    fun toggleFavorite_notFavorite_favorites() {
        every { EventRepository.isFavorite(1) } returns false
        every { EventRepository.favorited(1) } just Runs
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.toggleFavorite(sampleEvent()) { message = it }

        Assert.assertEquals("Evento favoritado!", message)
        verify { EventRepository.favorited(1) }
    }

    @Test
    fun toggleFavorite_alreadyFavorite_unfavorites() {
        every { EventRepository.isFavorite(1) } returns true
        every { EventRepository.unfavorite(1) } just Runs
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.toggleFavorite(sampleEvent()) { message = it }

        Assert.assertEquals("Evento desfavoritado!", message)
        verify { EventRepository.unfavorite(1) }
    }

    @Test
    fun signOut_clearsDataAndCallsCallback() {
        every { EventRepository.clearLocalData() } just Runs
        viewModel = HomeViewModel()
        var called = false

        viewModel.signOut { called = true }

        verify { auth.signOut() }
        verify { EventRepository.clearLocalData() }
        Assert.assertTrue(called)
    }

    @Test
    fun updateProfile_whenUserNull_callsOnComplete() {
        every { auth.currentUser } returns null
        viewModel = HomeViewModel()
        var completed = false

        viewModel.updateProfile("Novo Nome") { completed = true }

        Assert.assertTrue(completed)
    }

    @Test
    fun addComment_delegatesToRepository() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.addComment(1, "Texto", 5, "img", "audioPath", "videoPath", capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, "Comentário adicionado com sucesso!")
        }

        viewModel = HomeViewModel()
        var message: String? = null

        val mockAudioUri = mockk<Uri>()
        every { mockAudioUri.toString() } returns "audioPath"
        val mockVideoUri = mockk<Uri>()
        every { mockVideoUri.toString() } returns "videoPath"

        viewModel.addComment(1, "Texto", 5, "img", mockAudioUri, mockVideoUri) { message = it }

        Assert.assertEquals("Comentário adicionado com sucesso!", message)
    }

    @Test
    fun editComment_delegatesToRepository() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.editComment(1, "c1", "novo", 4, "audioPath", "img", "videoPath", capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, "Comentário editado com sucesso!")
        }

        viewModel = HomeViewModel()
        var message: String? = null

        val mockAudioUri = mockk<Uri>()
        every { mockAudioUri.toString() } returns "audioPath"
        val mockVideoUri = mockk<Uri>()
        every { mockVideoUri.toString() } returns "videoPath"

        viewModel.editComment(1, "c1", "novo", 4, mockAudioUri, "img", mockVideoUri) { message = it }

        Assert.assertEquals("Comentário editado com sucesso!", message)
    }

    @Test
    fun deleteComment_delegatesToRepository() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.deleteComment(1, "c1", capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, "Comentário excluído com sucesso!")
        }

        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.deleteComment(1, "c1") { message = it }

        Assert.assertEquals("Comentário excluído com sucesso!", message)
    }

    @Test
    fun rateEvent_delegatesToRepository() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.rateEvent(1, 5, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, "Avaliação salva com sucesso!")
        }

        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.rateEvent(1, 5) { message = it }

        Assert.assertEquals("Avaliação salva com sucesso!", message)
    }

    @Test
    fun createEvent_blankDescription_isRejected() {
        viewModel = HomeViewModel()
        var message: String? = null
        viewModel.createEvent("Título", "", "02/02/2000", "Local", "Cat") { message = it }
        Assert.assertEquals("Preencha todos os campos obrigatórios.", message)
        verify(exactly = 0) { EventRepository.createEvent(any()) }
    }

    @Test
    fun createEvent_blankDate_isRejected() {
        viewModel = HomeViewModel()
        var message: String? = null
        viewModel.createEvent("Título", "Desc", "", "Local", "Cat") { message = it }
        Assert.assertEquals("Preencha todos os campos obrigatórios.", message)
        verify(exactly = 0) { EventRepository.createEvent(any()) }
    }

    @Test
    fun createEvent_blankLocation_isRejected() {
        viewModel = HomeViewModel()
        var message: String? = null
        viewModel.createEvent("Título", "Desc", "02/02/2000", "", "Cat") { message = it }
        Assert.assertEquals("Preencha todos os campos obrigatórios.", message)
        verify(exactly = 0) { EventRepository.createEvent(any()) }
    }

    @Test
    fun addComment_nullMessageFallbacks() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.addComment(1, "Text", null, null, null, null, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, null)
        }

        viewModel = HomeViewModel()
        var messageSuccess: String? = null
        viewModel.addComment(1, "Text", null, null, null, null) { messageSuccess = it }
        Assert.assertEquals("Comentário adicionado!", messageSuccess)

        every {
            EventRepository.addComment(2, "Text2", null, null, null, null, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(false, null)
        }
        var messageError: String? = null
        viewModel.addComment(2, "Text2", null, null, null, null) { messageError = it }
        Assert.assertEquals("Erro ao adicionar comentário", messageError)
    }

    @Test
    fun editComment_nullMessageFallbacks() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.editComment(1, "c1", "Text", null, null, null, null, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, null)
        }

        viewModel = HomeViewModel()
        var messageSuccess: String? = null
        viewModel.editComment(1, "c1", "Text", null, null, null, null) { messageSuccess = it }
        Assert.assertEquals("Comentário atualizado!", messageSuccess)

        every {
            EventRepository.editComment(2, "c2", "Text2", null, null, null, null, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(false, null)
        }
        var messageError: String? = null
        viewModel.editComment(2, "c2", "Text2", null, null, null, null) { messageError = it }
        Assert.assertEquals("Erro ao editar", messageError)
    }

    @Test
    fun deleteComment_nullMessageFallbacks() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.deleteComment(1, "c1", capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, null)
        }

        viewModel = HomeViewModel()
        var messageSuccess: String? = null
        viewModel.deleteComment(1, "c1") { messageSuccess = it }
        Assert.assertEquals("Comentário excluído!", messageSuccess)

        every {
            EventRepository.deleteComment(2, "c2", capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(false, null)
        }
        var messageError: String? = null
        viewModel.deleteComment(2, "c2") { messageError = it }
        Assert.assertEquals("Erro ao excluir", messageError)
    }

    @Test
    fun rateEvent_nullMessageFallbacks() {
        val slotMessage = slot<(Boolean, String?) -> Unit>()
        every {
            EventRepository.rateEvent(1, 5, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(true, null)
        }

        viewModel = HomeViewModel()
        var messageSuccess: String? = null
        viewModel.rateEvent(1, 5) { messageSuccess = it }
        Assert.assertEquals("Avaliação salva!", messageSuccess)

        every {
            EventRepository.rateEvent(2, 4, capture(slotMessage))
        } answers {
            slotMessage.captured.invoke(false, null)
        }
        var messageError: String? = null
        viewModel.rateEvent(2, 4) { messageError = it }
        Assert.assertEquals("Erro ao avaliar", messageError)
    }

    @Test
    fun updateEvent_teacher_updatesEventSuccessfully() {
        every { EventRepository.userRole } returns "professor"
        every { EventRepository.updateEvent(any()) } just Runs
        every { EventRepository.allEvents } returns listOf(sampleEvent(id = 5))

        viewModel = HomeViewModel()
        viewModel.selectedEvent = sampleEvent(id = 5)
        var message: String? = null

        viewModel.updateEvent(5, "Título Novo", "Desc Nova", "03/03/2026", "Local Novo", "Geral", 10.0, 20.0, 50, "img") { message = it }

        Assert.assertEquals("Evento atualizado com sucesso!", message)
        verify { EventRepository.updateEvent(any()) }
        Assert.assertEquals("Título Novo", viewModel.selectedEvent?.title)
    }

    @Test
    fun updateEvent_nonTeacher_isRejected() {
        every { EventRepository.userRole } returns "aluno"
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.updateEvent(5, "t", "d", "01/01", "l", "c") { message = it }

        Assert.assertEquals("Apenas professores podem editar eventos.", message)
        verify(exactly = 0) { EventRepository.updateEvent(any()) }
    }

    @Test
    fun updateEvent_blankFields_isRejected() {
        every { EventRepository.userRole } returns "professor"
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.updateEvent(5, "", "d", "01/01", "l", "c") { message = it }

        Assert.assertEquals("Preencha todos os campos obrigatórios.", message)
        verify(exactly = 0) { EventRepository.updateEvent(any()) }
    }

    @Test
    fun updateEvent_notFound_isRejected() {
        every { EventRepository.userRole } returns "professor"
        every { EventRepository.allEvents } returns emptyList()
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.updateEvent(99, "t", "d", "01/01", "l", "c") { message = it }

        Assert.assertEquals("Evento não encontrado.", message)
        verify(exactly = 0) { EventRepository.updateEvent(any()) }
    }

    @Test
    fun endEvent_teacher_endsEventSuccessfully() {
        every { EventRepository.userRole } returns "professor"
        every { EventRepository.endEvent(5) } just Runs

        viewModel = HomeViewModel()
        viewModel.selectedEvent = sampleEvent(id = 5, isEnded = false)
        var message: String? = null

        viewModel.endEvent(5) { message = it }

        Assert.assertEquals("Evento encerrado com sucesso!", message)
        verify { EventRepository.endEvent(5) }
        Assert.assertTrue(viewModel.selectedEvent?.isEnded == true)
    }

    @Test
    fun endEvent_nonTeacher_isRejected() {
        every { EventRepository.userRole } returns "aluno"
        viewModel = HomeViewModel()
        var message: String? = null

        viewModel.endEvent(5) { message = it }

        Assert.assertEquals("Apenas professores podem encerrar eventos.", message)
        verify(exactly = 0) { EventRepository.endEvent(any()) }
    }

    @Test
    fun confirmPendingSpot_delegatesToRepository() {
        every { EventRepository.confirmPendingSpot(5, any()) } returns "Inscrição confirmada com sucesso!"
        viewModel = HomeViewModel()
        viewModel.selectedEvent = sampleEvent(id = 5)
        var message: String? = null

        viewModel.confirmPendingSpot(sampleEvent(id = 5)) { message = it }

        Assert.assertEquals("Inscrição confirmada com sucesso!", message)
        verify { EventRepository.confirmPendingSpot(5, any()) }
    }

    @Test
    fun declinePendingSpot_delegatesToRepository() {
        every { EventRepository.declinePendingSpot(5, any()) } returns "Vaga recusada."
        viewModel = HomeViewModel()
        viewModel.selectedEvent = sampleEvent(id = 5)
        var message: String? = null

        viewModel.declinePendingSpot(sampleEvent(id = 5)) { message = it }

        Assert.assertEquals("Vaga recusada.", message)
        verify { EventRepository.declinePendingSpot(5, any()) }
    }

    @Test
    fun hasPendingSpot_delegatesToRepository() {
        every { EventRepository.hasPendingSpot(5, any()) } returns true
        viewModel = HomeViewModel()

        val result = viewModel.hasPendingSpot(5)

        Assert.assertTrue(result)
        verify { EventRepository.hasPendingSpot(5, any()) }
    }

    @Test
    fun isOnWaitlist_delegatesToRepository() {
        every { EventRepository.isOnWaitlist(5, any()) } returns true
        viewModel = HomeViewModel()

        val result = viewModel.isOnWaitlist(5)

        Assert.assertTrue(result)
        verify { EventRepository.isOnWaitlist(5, any()) }
    }

    @Test
    fun getWaitlistPosition_delegatesToRepository() {
        every { EventRepository.getWaitlistPosition(5, any()) } returns 2
        viewModel = HomeViewModel()

        val result = viewModel.getWaitlistPosition(5)

        Assert.assertEquals(2, result)
        verify { EventRepository.getWaitlistPosition(5, any()) }
    }
}
