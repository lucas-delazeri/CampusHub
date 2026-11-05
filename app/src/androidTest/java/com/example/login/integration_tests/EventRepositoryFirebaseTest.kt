package com.example.login

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EventRepositoryFirebaseTest {

    companion object {
        private var isEmulator = false

        @BeforeClass
        @JvmStatic
        fun setupEmulators() {
            isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu")

            if (isEmulator) {
                val host = "192.168.3.9"

                try {
                    FirebaseAuth.getInstance().useEmulator(host, 9099)
                    FirebaseDatabase.getInstance().useEmulator(host, 9000)
                } catch (_: IllegalStateException) {
                }
            }
        }
    }

    private val auth = FirebaseAuth.getInstance()
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var uid: String

    @Before
    fun setUp() {

        val email = "teste_${UUID.randomUUID()}@teste.com"
        val result = Tasks.await(
            auth.createUserWithEmailAndPassword(email, "123456"),
            10, TimeUnit.SECONDS
        )
        uid = result.user!!.uid
        EventRepository.clearLocalData()
    }

    @After
    fun tearDown() {
        if (isEmulator) {
            EventRepository.clearLocalData()
            auth.signOut()
        }
    }

    private fun read(path: String): DataSnapshot =
        Tasks.await(db.child(path).get(), 10, TimeUnit.SECONDS)

    private fun eventually(timeoutMs: Long = 8000, condition: () -> Boolean) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            try {
                if (condition()) return
            } catch (_: Exception) {
            }
            Thread.sleep(200)
        }
        throw AssertionError("Condição não atendida em ${timeoutMs}ms")
    }

    private fun awaitResult(block: ((Boolean, String?) -> Unit) -> Unit): Pair<Boolean, String?> {
        val latch = CountDownLatch(1)
        var ok = false
        var msg: String? = null
        block { success, message ->
            ok = success
            msg = message
            latch.countDown()
        }
        assertTrue("Callback não retornou", latch.await(10, TimeUnit.SECONDS))
        return ok to msg
    }

    @Test
    fun createEvent_gravaEventoNoRealtimeDatabase() {
        val event = Event(
            id = 9001,
            title = "Evento de Teste",
            description = "Descrição",
            date = "25 de Out, 14:00",
            location = "Auditório A",
            organizer = "Organizador",
            category = "Tecnologia"
        )

        EventRepository.createEvent(event)

        eventually { read("events/9001").exists() }
        assertEquals("Evento de Teste", read("events/9001/title").value)
        assertEquals("Tecnologia", read("events/9001/category").value)
    }

    @Test
    fun enroll_salvaInscricaoNoPerfilDoUsuario() {
        EventRepository.enroll(3)

        eventually { read("users/$uid/enrolledEvents").exists() }
        val list = read("users/$uid/enrolledEvents").value as List<*>
        assertTrue(list.contains(3L))
        assertTrue(EventRepository.isEnrolled(3))
    }

    @Test
    fun unenroll_removeInscricaoDoBanco() {
        EventRepository.enroll(3)
        EventRepository.enroll(4)
        eventually { (read("users/$uid/enrolledEvents").value as? List<*>)?.size == 2 }

        EventRepository.unenroll(3)

        eventually {
            val list = read("users/$uid/enrolledEvents").value as? List<*>
            list != null && !list.contains(3L) && list.contains(4L)
        }
        assertFalse(EventRepository.isEnrolled(3))
    }

    @Test
    fun loadUserEnrollments_leInscricoesGravadasNoBanco() {
        Tasks.await(db.child("users/$uid/enrolledEvents").setValue(listOf(1, 5)), 10, TimeUnit.SECONDS)

        EventRepository.loadUserEnrollments()

        eventually { EventRepository.enrolledEventIds == setOf(1, 5) }
    }

    @Test
    fun favorited_e_unfavorite_sincronizamComBanco() {
        EventRepository.favorited(2)
        eventually { (read("users/$uid/favoriteEvents").value as? List<*>)?.contains(2L) == true }
        assertTrue(EventRepository.isFavorite(2))

        EventRepository.unfavorite(2)
        eventually { !read("users/$uid/favoriteEvents").exists() }
        assertFalse(EventRepository.isFavorite(2))
    }

    @Test
    fun loadUserRole_leRoleDoBanco() {
        Tasks.await(db.child("users/$uid/role").setValue("organizador"), 10, TimeUnit.SECONDS)

        EventRepository.loadUserRole()

        eventually { EventRepository.userRole == "organizador" }
    }

    @Test
    fun addComment_gravaComentarioComDadosDoAutor() {
        val eventId = 9991
        Tasks.await(db.child("events/$eventId/comments").removeValue())

        val (ok, _) = awaitResult { cb ->
            EventRepository.addComment(eventId, "Ótimo evento!", 5, null, null, null, cb)
        }
        assertTrue(ok)

        val comments = read("events/$eventId/comments")
        assertEquals(1L, comments.childrenCount)
        val comment = comments.children.first()
        assertEquals("Ótimo evento!", comment.child("content").value)
        assertEquals(uid, comment.child("authorId").value)
    }

    @Test
    fun editComment_atualizaConteudoENota() {
        val eventId = 9992
        Tasks.await(db.child("events/$eventId/comments").removeValue())

        awaitResult { cb -> EventRepository.addComment(eventId, "Original", 3, null, null, null, cb) }
        val commentId = read("events/$eventId/comments").children.first().key!!

        val (ok, _) = awaitResult { cb ->
            EventRepository.editComment(eventId, commentId, "Editado", 4, null, null, null, cb)
        }
        assertTrue(ok)

        val updated = read("events/$eventId/comments/$commentId")
        assertEquals("Editado", updated.child("content").value)
        assertEquals(4L, updated.child("rating").value)
    }

    @Test
    fun deleteComment_removeComentarioDoBanco() {
        val eventId = 9993
        Tasks.await(db.child("events/$eventId/comments").removeValue())

        awaitResult { cb -> EventRepository.addComment(eventId, "Para apagar", null, null, null, null, cb) }
        val commentId = read("events/$eventId/comments").children.first().key!!

        val (ok, _) = awaitResult { cb -> EventRepository.deleteComment(eventId, commentId, cb) }

        assertTrue(ok)
        assertFalse(read("events/$eventId/comments/$commentId").exists())
    }

    @Test
    fun rateEvent_salvaNotaEListenToRatingsCalculaMedia() {
        val eventId = 9994
        Tasks.await(db.child("events/$eventId/ratings").removeValue())

        val (ok, _) = awaitResult { cb -> EventRepository.rateEvent(eventId, 4, cb) }
        assertTrue(ok)
        assertEquals(4L, read("events/$eventId/ratings/$uid").value)

        var average = 0.0
        var total = 0
        var mine: Int? = null
        val latch = CountDownLatch(1)
        val listener = EventRepository.listenToRatings(eventId) { avg, count, userRating ->
            average = avg
            total = count
            mine = userRating
            latch.countDown()
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        EventRepository.removeRatingsListener(eventId, listener)

        assertEquals(4.0, average, 0.01)
        assertEquals(1, total)
        assertEquals(4, mine)
    }
}
