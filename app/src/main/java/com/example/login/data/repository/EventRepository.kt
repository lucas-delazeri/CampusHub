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
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    val allEvents = listOf(
        Event(
            id = 1,
            title = "II Feira de Profissões e Carreiras",
            description = "Conecte-se com grandes empresas do mercado, participe de workshops de currículo e descubra novas oportunidades de estágio e emprego no ecossistema universitário.",
            date = "24 de Setembro, 14:00",
            location = "Auditório Central - Bloco A",
            organizer = "Diretório Acadêmico",
            category = "Carreira",
            isEnded = true
        ),
        Event(
            id = 2,
            title = "Hackathon CampusHub 2026",
            description = "Uma maratona de 48 horas de programação, ideação e design. Venha desenvolver soluções tecnológicas inovadoras para melhorar a vida estudantil no campus.",
            date = "02 a 04 de Outubro, 18:00",
            location = "Laboratório de Informática Avançada",
            organizer = "Faculdade de Computação",
            category = "Tecnologia"
        ),
        Event(
            id = 3,
            title = "Workshop de Introdução à Inteligência Artificial",
            description = "Aprenda os conceitos fundamentais de Machine Learning e Redes Neurais na prática. Traga seu notebook e prepare-se para codificar seus primeiros modelos.",
            date = "10 de Outubro, 09:00",
            location = "Sala 102 - Bloco de Engenharia",
            organizer = "IEEE Student Branch",
            category = "Tecnologia"
        ),
        Event(
            id = 4,
            title = "Festival Universitário de Música e Cultura",
            description = "Apresentações musicais com bandas formadas por alunos, praça de alimentação com food trucks, exposições de artes visuais e muito mais para integrar a comunidade.",
            date = "15 de Outubro, 17:00",
            location = "Praça de Convivência Externa",
            organizer = "Secretaria de Cultura Estudantil",
            category = "Cultura"
        ),
        Event(
            id = 5,
            title = "Palestra: Saúde Mental na Vida Acadêmica",
            description = "Estratégias fundamentais de mindfulness, gestão de tempo e inteligência emocional para lidar com o estresse dos exames e manter o equilíbrio na rotina universitária.",
            date = "22 de Outubro, 10:30",
            location = "Anfiteatro da Biblioteca Central",
            organizer = "Departamento de Psicologia",
            category = "Saúde"
        )
    )

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

    fun enroll(eventId: Int) {
        enrolledEventIds = enrolledEventIds + eventId
        syncWithDatabase()
    }

    fun unenroll(eventId: Int) {
        enrolledEventIds = enrolledEventIds - eventId
        syncWithDatabase()
    }

    fun isEnrolled(eventId: Int): Boolean {
        return enrolledEventIds.contains(eventId)
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

    fun addComment(eventId: Int, content: String, rating: Int? = null, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Usuário não autenticado")
            return
        }

        if (content.isBlank()) {
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

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val comment = Comment(
            id = commentId,
            eventId = eventId,
            authorId = user.uid,
            authorName = authorName,
            content = content.trim(),
            publishedAt = dateStr,
            rating = rating
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
            "rating" to newRating
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