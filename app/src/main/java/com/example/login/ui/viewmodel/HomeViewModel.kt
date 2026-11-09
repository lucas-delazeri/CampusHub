package com.example.login.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class HomeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var currentTab by mutableStateOf(0)
    var selectedEvent by mutableStateOf<Event?>(null)
    var isCreatingEvent by mutableStateOf(false)
    var editingEvent by mutableStateOf<Event?>(null)
    var userName by mutableStateOf(auth.currentUser?.displayName ?: "Estudante")
    val email: String = auth.currentUser?.email ?: "No email"

    init {
        EventRepository.loadUserEnrollments()
        EventRepository.loadUserFavoriteEvents()
        EventRepository.loadUserRole()
        EventRepository.listenToEvents()
    }

    val userRole: String
        get() = EventRepository.userRole

    val isTeacher: Boolean
        get() = userRole.equals("professor", ignoreCase = true)

    fun createEvent(
        title: String,
        description: String,
        date: String,
        location: String,
        category: String,
        lat: Double? = null,
        lng: Double? = null,
        maxCapacity: Int = 0,
        imageBase64: String? = null,
        isEnded: Boolean = false,
        onResultMessage: (String) -> Unit
    ) {
        if (!isTeacher) {
            onResultMessage("Apenas professores podem criar eventos.")
            return
        }
        if (title.isBlank() || description.isBlank() || date.isBlank() || location.isBlank()) {
            onResultMessage("Preencha todos os campos obrigatórios.")
            return
        }
        val newId = (EventRepository.allEvents.maxOfOrNull { it.id } ?: 0) + 1
        val newEvent = Event(
            id = newId,
            title = title,
            description = description,
            date = date,
            location = location,
            organizer = userName,
            category = if (category.isBlank()) "Geral" else category,
            isEnded = isEnded,
            latitude = lat ?: -23.550520,
            longitude = lng ?: -46.633308,
            maxCapacity = maxCapacity,
            imageBase64 = imageBase64
        )
        EventRepository.createEvent(newEvent)
        onResultMessage("Evento criado com sucesso!")
    }

    fun updateEvent(
        eventId: Int,
        title: String,
        description: String,
        date: String,
        location: String,
        category: String,
        lat: Double? = null,
        lng: Double? = null,
        maxCapacity: Int = 0,
        imageBase64: String? = null,
        isEnded: Boolean = false,
        onResultMessage: (String) -> Unit
    ) {
        if (!isTeacher) {
            onResultMessage("Apenas professores podem editar eventos.")
            return
        }
        if (title.isBlank() || description.isBlank() || date.isBlank() || location.isBlank()) {
            onResultMessage("Preencha todos os campos obrigatórios.")
            return
        }
        val currentEvent = EventRepository.allEvents.find { it.id == eventId } ?: run {
            onResultMessage("Evento não encontrado.")
            return
        }
        val updated = currentEvent.copy(
            title = title,
            description = description,
            date = date,
            location = location,
            category = if (category.isBlank()) "Geral" else category,
            latitude = lat ?: currentEvent.latitude,
            longitude = lng ?: currentEvent.longitude,
            maxCapacity = maxCapacity,
            imageBase64 = imageBase64 ?: currentEvent.imageBase64,
            isEnded = isEnded
        )
        EventRepository.updateEvent(updated)
        if (selectedEvent?.id == eventId) {
            selectedEvent = updated
        }
        editingEvent = null
        onResultMessage("Evento atualizado com sucesso!")
    }

    fun endEvent(eventId: Int, onResultMessage: (String) -> Unit) {
        if (!isTeacher) {
            onResultMessage("Apenas professores podem encerrar eventos.")
            return
        }
        EventRepository.endEvent(eventId)
        if (selectedEvent?.id == eventId) {
            selectedEvent = selectedEvent?.copy(isEnded = true)
        }
        onResultMessage("Evento encerrado com sucesso!")
    }

    fun changeTab(tabIndex: Int) {
        currentTab = tabIndex
    }

    fun isEventEnrolled(eventId: Int): Boolean {
        return EventRepository.isEnrolled(eventId)
    }

    fun hasPendingSpot(eventId: Int): Boolean {
        val currentUserId = auth.currentUser?.uid ?: email
        return EventRepository.hasPendingSpot(eventId, currentUserId)
    }

    fun isOnWaitlist(eventId: Int): Boolean {
        val currentUserId = auth.currentUser?.uid ?: email
        return EventRepository.isOnWaitlist(eventId, currentUserId)
    }

    fun getWaitlistPosition(eventId: Int): Int {
        val currentUserId = auth.currentUser?.uid ?: email
        return EventRepository.getWaitlistPosition(eventId, currentUserId)
    }

    fun confirmPendingSpot(event: Event, onResultMessage: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: email
        val message = EventRepository.confirmPendingSpot(event.id, currentUserId)
        if (selectedEvent?.id == event.id) {
            selectedEvent = EventRepository.allEvents.find { it.id == event.id } ?: selectedEvent
        }
        onResultMessage(message)
    }

    fun declinePendingSpot(event: Event, onResultMessage: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: email
        val message = EventRepository.declinePendingSpot(event.id, currentUserId)
        if (selectedEvent?.id == event.id) {
            selectedEvent = EventRepository.allEvents.find { it.id == event.id } ?: selectedEvent
        }
        onResultMessage(message)
    }

    fun toggleEnrollment(event: Event, onResultMessage: (String) -> Unit) {
        if (event.isEnded) {
            onResultMessage("Este evento já encerrou. Ações de inscrição estão desativadas.")
            return
        }
        val currentUserId = auth.currentUser?.uid ?: email
        val message = EventRepository.enrollUser(event.id, currentUserId)
        if (selectedEvent?.id == event.id) {
            selectedEvent = EventRepository.allEvents.find { it.id == event.id } ?: selectedEvent
        }
        onResultMessage(message)
    }

    fun toggleFavorite(event: Event, onResultMessage: (String) -> Unit) {
        if (EventRepository.isFavorite(event.id)) {
            EventRepository.unfavorite(event.id)
            onResultMessage("Evento desfavoritado!")
        } else {
            EventRepository.favorited(event.id)
            onResultMessage("Evento favoritado!")
        }
    }

    fun updateProfile(newName: String, onComplete: () -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userName = newName
                }
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        auth.signOut()
        EventRepository.clearLocalData()
        onSignOutComplete()
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun addComment(
        eventId: Int,
        content: String,
        rating: Int?,
        imageBase64: String?,
        audioUri: Uri?,
        videoUri: Uri?,
        onMessage: (String) -> Unit
    ) {
        val audioPath = audioUri?.toString()
        val videoPath = videoUri?.toString()
        EventRepository.addComment(eventId, content, rating, imageBase64, audioPath, videoPath) { success, message ->
            onMessage(message ?: if (success) "Comentário adicionado!" else "Erro ao adicionar comentário")
        }
    }

    fun editComment(
        eventId: Int,
        commentId: String,
        newContent: String,
        newRating: Int?,
        newAudioUri: Uri?,
        newImageBase64: String? = null,
        newVideoUri: Uri? = null,
        onResultMessage: (String) -> Unit
    ) {
        val audioPath = newAudioUri?.toString()
        val videoPath = newVideoUri?.toString()
        EventRepository.editComment(eventId, commentId, newContent, newRating, audioPath, newImageBase64, videoPath) { success, message ->
            onResultMessage(message ?: if (success) "Comentário atualizado!" else "Erro ao editar")
        }
    }

    fun deleteComment(eventId: Int, commentId: String, onResultMessage: (String) -> Unit) {
        EventRepository.deleteComment(eventId, commentId) { success, message ->
            onResultMessage(message ?: if (success) "Comentário excluído!" else "Erro ao excluir")
        }
    }

    fun rateEvent(eventId: Int, rating: Int, onResultMessage: (String) -> Unit) {
        EventRepository.rateEvent(eventId, rating) { success, message ->
            onResultMessage(message ?: if (success) "Avaliação salva!" else "Erro ao avaliar")
        }
    }
}