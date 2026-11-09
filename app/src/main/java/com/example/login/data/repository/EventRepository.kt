package com.example.login.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.login.data.model.Comment
import com.example.login.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EventRepository {
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val defaultEventsList = listOf(
        Event(
            id = 1,
            title = "II Feira de Profissões e Carreiras",
            description = "Conecte-se com grandes empresas do mercado, participe de workshops de currículo e descubra novas oportunidades de estágio e emprego no ecossistema universitário.",
            date = "24 de Setembro, 14:00",
            location = "Auditório Central - Bloco A",
            organizer = "Diretório Acadêmico",
            category = "Carreira",
            isEnded = true,
            latitude = -23.550520,
            longitude = -46.633308
        ),
        Event(
            id = 2,
            title = "Hackathon CampusHub 2026",
            description = "Uma maratona de 48 horas de programação, ideação e design. Venha desenvolver soluções tecnológicas inovadoras para melhorar a vida estudantil no campus.",
            date = "02 a 04 de Outubro, 18:00",
            location = "Laboratório de Informática Avançada",
            organizer = "Faculdade de Computação",
            category = "Tecnologia",
            isEnded = true,
            latitude = -23.551520,
            longitude = -46.634308
        ),
        Event(
            id = 3,
            title = "Workshop de Introdução à Inteligência Artificial",
            description = "Aprenda os conceitos fundamentais de Machine Learning e Redes Neurais na prática. Traga seu notebook e prepare-se para codificar seus primeiros modelos.",
            date = "10 de Outubro, 09:00",
            location = "Sala 102 - Bloco de Engenharia",
            organizer = "IEEE Student Branch",
            category = "Tecnologia",
            latitude = -23.549520,
            longitude = -46.632308
        ),
        Event(
            id = 4,
            title = "Festival Universitário de Música e Cultura",
            description = "Apresentações musicais com bandas formadas por alunos, praça de alimentação com food trucks, exposições de artes visuais e muito mais para integrar a comunidade.",
            date = "15 de Outubro, 17:00",
            location = "Praça de Convivência Externa",
            organizer = "Secretaria de Cultura Estudantil",
            category = "Cultura",
            latitude = -23.552520,
            longitude = -46.635308
        ),
        Event(
            id = 5,
            title = "Palestra: Saúde Mental na Vida Acadêmica",
            description = "Estratégias fundamentais de mindfulness, gestão de tempo e inteligência emocional para lidar com o estresse dos exames e manter o equilíbrio na rotina universitária.",
            date = "22 de Outubro, 10:30",
            location = "Anfiteatro da Biblioteca Central",
            organizer = "Departamento de Psicologia",
            category = "Saúde",
            latitude = -23.548520,
            longitude = -46.631308
        )
    )

    var allEvents by mutableStateOf(defaultEventsList)
        private set

    var userRole by mutableStateOf("professor")

    fun listenToEvents() {
        db.child("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firebaseList = mutableListOf<Event>()
                for (child in snapshot.children) {
                    val event = child.getValue(Event::class.java)
                    if (event != null && event.title.isNotBlank() && event.id > 0) {
                        firebaseList.add(event)
                    }
                }
                if (firebaseList.isNotEmpty()) {
                    val firebaseIds = firebaseList.map { it.id }.toSet()
                    val merged = defaultEventsList.filter { it.id !in firebaseIds } + firebaseList
                    allEvents = merged.sortedBy { it.id }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadUserRole() {
        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("role")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userRole = snapshot.value as? String ?: userRole
                }
            }
    }

    fun createEvent(newEvent: Event) {
        allEvents = allEvents + newEvent
        db.child("events").child(newEvent.id.toString()).setValue(newEvent)
    }

    fun updateEvent(updatedEvent: Event) {
        allEvents = allEvents.map { if (it.id == updatedEvent.id) updatedEvent else it }
        db.child("events").child(updatedEvent.id.toString()).setValue(updatedEvent)
    }

    fun endEvent(eventId: Int) {
        allEvents = allEvents.map { if (it.id == eventId) it.copy(isEnded = true) else it }
        db.child("events").child(eventId.toString()).child("isEnded").setValue(true)
    }

    var enrolledEventIds by mutableStateOf(setOf<Int>())
        private set

    fun loadUserEnrollments() {
        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("enrolledEvents")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val list = snapshot.value as? List<*>
                    enrolledEventIds = list?.filterIsInstance<Long>()?.map { it.toInt() }?.toSet() ?: emptySet()
                }
            }
    }

    private fun syncWithDatabase() {
        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("enrolledEvents")
            .setValue(enrolledEventIds.toList())
    }

    fun enrollUser(eventId: Int, userId: String): String {
        val event = allEvents.find { it.id == eventId } ?: return "Evento não encontrado."

        if (event.isEnded) {
            return "Este evento já encerrou. Inscrições desativadas."
        }

        val isCurrentlyEnrolled = event.enrolledUserIds.contains(userId) || enrolledEventIds.contains(eventId)

        if (isCurrentlyEnrolled) {
            // Cancel enrollment and offer spot to next student on waitlist if available
            val newEnrolled = event.enrolledUserIds - userId
            var newWaitlist = event.waitlistUserIds
            var newPending = event.pendingConfirmationUserIds

            if (newWaitlist.isNotEmpty() && (event.maxCapacity == 0 || (newEnrolled.size + newPending.size) < event.maxCapacity)) {
                val promotedUser = newWaitlist.first()
                newWaitlist = newWaitlist.drop(1)
                newPending = newPending + promotedUser
                val updated = event.copy(
                    enrolledUserIds = newEnrolled,
                    waitlistUserIds = newWaitlist,
                    pendingConfirmationUserIds = newPending
                )
                updateEvent(updated)
            } else {
                val updated = event.copy(enrolledUserIds = newEnrolled, waitlistUserIds = newWaitlist)
                updateEvent(updated)
            }

            enrolledEventIds = enrolledEventIds - eventId
            syncWithDatabase()
            return "Inscrição cancelada com sucesso! A vaga foi oferecida para o próximo aluno da lista de espera."
        }

        if (event.pendingConfirmationUserIds.contains(userId)) {
            return confirmPendingSpot(eventId, userId)
        }

        if (event.waitlistUserIds.contains(userId)) {
            // Leave waitlist
            val newWaitlist = event.waitlistUserIds - userId
            val updated = event.copy(waitlistUserIds = newWaitlist)
            updateEvent(updated)
            return "Você saiu da lista de espera."
        }

        if (event.isFull) {
            // Event is full -> Join waitlist
            val newWaitlist = event.waitlistUserIds + userId
            val position = newWaitlist.size
            val updated = event.copy(waitlistUserIds = newWaitlist)
            updateEvent(updated)
            return "Vagas esgotadas! Você entrou na Lista de Espera na posição #$position."
        }

        val newEnrolled = event.enrolledUserIds + userId
        val updated = event.copy(enrolledUserIds = newEnrolled)
        updateEvent(updated)
        enrolledEventIds = enrolledEventIds + eventId
        syncWithDatabase()
        return "Inscrição realizada com sucesso!"
    }

    fun confirmPendingSpot(eventId: Int, userId: String): String {
        val event = allEvents.find { it.id == eventId } ?: return "Evento não encontrado."

        val newPending = event.pendingConfirmationUserIds - userId
        val newEnrolled = event.enrolledUserIds + userId
        val updated = event.copy(
            enrolledUserIds = newEnrolled,
            pendingConfirmationUserIds = newPending
        )
        updateEvent(updated)
        enrolledEventIds = enrolledEventIds + eventId
        syncWithDatabase()
        return "Inscrição confirmada com sucesso! Seja bem-vindo ao evento."
    }

    fun declinePendingSpot(eventId: Int, userId: String): String {
        val event = allEvents.find { it.id == eventId } ?: return "Evento não encontrado."

        val newPending = event.pendingConfirmationUserIds - userId
        var newWaitlist = event.waitlistUserIds
        var finalPending = newPending

        if (newWaitlist.isNotEmpty() && (event.maxCapacity == 0 || (event.currentEnrolledCount + finalPending.size) < event.maxCapacity)) {
            val promotedUser = newWaitlist.first()
            newWaitlist = newWaitlist.drop(1)
            finalPending = finalPending + promotedUser
        }

        val updated = event.copy(
            waitlistUserIds = newWaitlist,
            pendingConfirmationUserIds = finalPending
        )
        updateEvent(updated)
        return "Vaga recusada. A vaga foi disponibilizada para o próximo aluno da lista."
    }

    fun hasPendingSpot(eventId: Int, userId: String): Boolean {
        val event = allEvents.find { it.id == eventId } ?: return false
        return event.pendingConfirmationUserIds.contains(userId)
    }

    fun performCheckIn(eventId: Int, targetUserId: String): String {
        val event = allEvents.find { it.id == eventId } ?: return "Evento não encontrado."

        if (event.checkedInUserIds.contains(targetUserId)) {
            return "Atenção: Presença de '$targetUserId' já foi registrada anteriormente!"
        }

        val newCheckedIn = event.checkedInUserIds + targetUserId
        val updated = event.copy(checkedInUserIds = newCheckedIn)
        updateEvent(updated)
        return "Check-in realizado com sucesso! Presença de '$targetUserId' confirmada."
    }

    fun performCheckInFromPayload(payload: String): String {
        val parsed = com.example.login.data.utils.QrCodeUtils.parseCheckInPayload(payload)
            ?: return "Código QR inválido ou formato não reconhecido."
        return performCheckIn(parsed.first, parsed.second)
    }

    fun isUserCheckedIn(eventId: Int, userId: String): Boolean {
        val event = allEvents.find { it.id == eventId } ?: return false
        return event.checkedInUserIds.contains(userId)
    }

    fun enroll(eventId: Int) {
        val userId = auth.currentUser?.uid ?: "user_id"
        enrollUser(eventId, userId)
    }

    fun unenroll(eventId: Int) {
        val userId = auth.currentUser?.uid ?: "user_id"
        enrollUser(eventId, userId)
    }

    fun isEnrolled(eventId: Int): Boolean {
        val currentUserId = auth.currentUser?.uid
        val event = allEvents.find { it.id == eventId }
        return enrolledEventIds.contains(eventId) || (currentUserId != null && event?.enrolledUserIds?.contains(currentUserId) == true)
    }

    fun isOnWaitlist(eventId: Int, userId: String): Boolean {
        val event = allEvents.find { it.id == eventId } ?: return false
        return event.waitlistUserIds.contains(userId)
    }

    fun getWaitlistPosition(eventId: Int, userId: String): Int {
        val event = allEvents.find { it.id == eventId } ?: return 0
        val index = event.waitlistUserIds.indexOf(userId)
        return if (index >= 0) index + 1 else 0
    }

    var favoriteEventsId by mutableStateOf(setOf<Int>())
        private set

    fun loadUserFavoriteEvents() {
        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("favoriteEvents")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val list = snapshot.value as? List<*>
                    favoriteEventsId = list?.filterIsInstance<Long>()?.map { it.toInt() }?.toSet() ?: emptySet()
                }
            }
    }

    private fun syncFavoriteEventsWithDatabase() {
        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("favoriteEvents")
            .setValue(favoriteEventsId.toList())
    }

    fun favorited(eventId: Int) {
        favoriteEventsId = favoriteEventsId + eventId
        syncFavoriteEventsWithDatabase()
    }

    fun unfavorite(eventId: Int) {
        favoriteEventsId = favoriteEventsId - eventId
        syncFavoriteEventsWithDatabase()
    }

    fun isFavorite(eventId: Int): Boolean {
        return favoriteEventsId.contains(eventId)
    }

    fun clearLocalData() {
        allEvents = defaultEventsList
        enrolledEventIds = emptySet()
        favoriteEventsId = emptySet()
    }

    fun listenToComments(eventId: Int, onCommentsChanged: (List<Comment>) -> Unit): ValueEventListener {
        val commentsRef = db.child("events").child(eventId.toString()).child("comments")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Comment>()
                for (child in snapshot.children) {
                    val comment = child.getValue(Comment::class.java)
                    if (comment != null) {
                        list.add(comment)
                    }
                }
                onCommentsChanged(list)
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        }
        commentsRef.addValueEventListener(listener)
        return listener
    }

    fun removeCommentsListener(eventId: Int, listener: ValueEventListener) {
        db.child("events").child(eventId.toString()).child("comments").removeEventListener(listener)
    }

    fun addComment(
        eventId: Int,
        content: String,
        rating: Int?,
        imageBase64: String?,
        audioUri: String?,
        videoUrl: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Usuário não autenticado")
            return
        }
        if (content.isBlank() && imageBase64 == null && audioUri == null) {
            onResult(false, "O comentário não pode ser vazio")
            return
        }

        val commentsRef = db.child("events").child(eventId.toString()).child("comments")
        val commentId = commentsRef.push().key ?: run {
            onResult(false, "Erro ao gerar ID do comentário")
            return
        }

        val authorName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "Estudante"

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val comment = Comment(
            id = commentId,
            eventId = eventId,
            authorId = user.uid,
            authorName = authorName,
            content = content.trim(),
            publishedAt = dateStr,
            rating = rating,
            imageBase64 = imageBase64,
            audioUri = audioUri,
            videoUrl = videoUrl
        )

        commentsRef.child(commentId).setValue(comment)
            .addOnSuccessListener { onResult(true, "Comentário adicionado!") }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun editComment(
        eventId: Int,
        commentId: String,
        newContent: String,
        newRating: Int?,
        newAudioUri: String?,
        newImageBase64: String? = null,
        newVideoUrl: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Usuário não autenticado")
            return
        }

        if (newContent.isBlank()) {
            onResult(false, "O comentário não pode ser vazio")
            return
        }

        val commentRef = db.child("events")
            .child(eventId.toString())
            .child("comments")
            .child(commentId)

        val updates = mapOf<String, Any?>(
            "content" to newContent.trim(),
            "rating" to newRating,
            "audioUri" to newAudioUri,
            "imageBase64" to newImageBase64,
            "videoUrl" to newVideoUrl
        )

        commentRef.updateChildren(updates)
            .addOnSuccessListener { onResult(true, "Comentário editado!") }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun deleteComment(eventId: Int, commentId: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Usuário não autenticado")
            return
        }

        val commentRef = db.child("events").child(eventId.toString()).child("comments").child(commentId)
        commentRef.removeValue()
            .addOnSuccessListener { onResult(true, "Comentário excluído!") }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun listenToRatings(
        eventId: Int,
        onRatingsChanged: (average: Double, totalCount: Int, userRating: Int?) -> Unit
    ): ValueEventListener {
        val ratingsRef = db.child("events").child(eventId.toString()).child("ratings")
        val currentUserId = auth.currentUser?.uid
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var sum = 0
                var count = 0
                var userRating: Int? = null

                for (child in snapshot.children) {
                    val rating = child.getValue(Int::class.java)
                    if (rating != null) {
                        sum += rating
                        count++
                        if (child.key == currentUserId) {
                            userRating = rating
                        }
                    }
                }

                val avg = if (count > 0) sum.toDouble() / count else 0.0
                onRatingsChanged(avg, count, userRating)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        ratingsRef.addValueEventListener(listener)
        return listener
    }

    fun removeRatingsListener(eventId: Int, listener: ValueEventListener) {
        db.child("events").child(eventId.toString()).child("ratings").removeEventListener(listener)
    }

    fun rateEvent(eventId: Int, rating: Int, onResult: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(false, "Usuário não autenticado")
            return
        }
        if (rating !in 1..5) {
            onResult(false, "Nota inválida")
            return
        }

        db.child("events").child(eventId.toString()).child("ratings").child(userId)
            .setValue(rating)
            .addOnSuccessListener { onResult(true, "Avaliação salva com sucesso!") }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }
}