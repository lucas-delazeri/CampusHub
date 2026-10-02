package com.example.login.ui.viewmodel

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
    var userName by mutableStateOf(auth.currentUser?.displayName ?: "Estudante")
    val email: String = auth.currentUser?.email ?: "No email"

    init {
        EventRepository.loadUserEnrollments()
        EventRepository.loadUserFavoriteEvents()
    }

    fun changeTab(tabIndex: Int) {
        currentTab = tabIndex
    }

    fun isEventEnrolled(eventId: Int): Boolean {
        return EventRepository.isEnrolled(eventId)
    }

    fun toggleEnrollment(event: Event, onResultMessage: (String) -> Unit) {
        if (EventRepository.isEnrolled(event.id)) {
            EventRepository.unenroll(event.id)
            onResultMessage("Inscrição cancelada com sucesso!")
        } else {
            EventRepository.enroll(event.id)
            onResultMessage("Inscrição realizada com sucesso!")
        }
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

    fun addComment(eventId: Int, content: String, onResultMessage: (String) -> Unit) {
        EventRepository.addComment(eventId, content) { success, message ->
            onResultMessage(message ?: if (success) "Comentário adicionado!" else "Erro ao comentar")
        }
    }

    fun editComment(eventId: Int, commentId: String, newContent: String, onResultMessage: (String) -> Unit) {
        EventRepository.editComment(eventId, commentId, newContent) { success, message ->
            onResultMessage(message ?: if (success) "Comentário atualizado!" else "Erro ao editar")
        }
    }

    fun deleteComment(eventId: Int, commentId: String, onResultMessage: (String) -> Unit) {
        EventRepository.deleteComment(eventId, commentId) { success, message ->
            onResultMessage(message ?: if (success) "Comentário excluído!" else "Erro ao excluir")
        }
    }
}
