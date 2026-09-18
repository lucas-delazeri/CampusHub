package com.example.login.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.login.data.Event
import com.example.login.data.EventRepository
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
}
