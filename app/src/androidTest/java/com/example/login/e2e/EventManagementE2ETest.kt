package com.example.login.e2e

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.HomeActivity
import com.example.login.data.repository.EventRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EventManagementE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun setUp() {
        val auth = FirebaseAuth.getInstance()
        try {
            val email = "e2e_prof_${UUID.randomUUID().toString().take(6)}@test.com"
            val result = Tasks.await(auth.createUserWithEmailAndPassword(email, "Profe123"), 10, TimeUnit.SECONDS)
            val uid = result.user!!.uid
            Tasks.await(FirebaseDatabase.getInstance().reference.child("users").child(uid).child("role").setValue("professor"), 10, TimeUnit.SECONDS)
        } catch (_: Exception) {
        }
        EventRepository.clearLocalData()
        EventRepository.userRole = "professor"
    }

    @After
    fun tearDown() {
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun e2e_createEventAndNavigateDetailsFlow() {
        composeTestRule.runOnUiThread {
            EventRepository.userRole = "professor"
        }

        composeTestRule.onNodeWithContentDescription("Criar Evento")
            .performClick()

        // 1. Título do Evento
        composeTestRule.onNode(hasText("Título do Evento") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Simpósio E2E")

        // 2. Capacidade Máxima
        composeTestRule.onNode(hasText("Capacidade Máxima") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("100")

        // 3. Categoria
        composeTestRule.onNode(hasText("Categoria") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Acadêmico")

        // 4. Data e Horário
        composeTestRule.onNode(hasText("Data e Horário") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("30 de Nov, 10:00")

        // 5. Nome do Local / Endereço
        composeTestRule.onNode(hasText("Nome do Local / Endereço") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Bloco Central")

        // 6. Descrição Completa
        composeTestRule.onNode(hasText("Descrição Completa") and hasSetTextAction())
            .performScrollTo()
            .performTextInput("Descrição do evento criado via E2E test")

        // Esconde o teclado virtual do emulador
        try {
            closeSoftKeyboard()
        } catch (_: Exception) {
        }

        composeTestRule.runOnUiThread {
            EventRepository.userRole = "professor"
        }

        composeTestRule.onNodeWithText("Publicar Evento")
            .performScrollTo()
            .performClick()

        // Aguarda a criação do evento no repositório em memória
        composeTestRule.waitUntil(10_000) {
            EventRepository.allEvents.any { it.title == "Simpósio E2E" }
        }

        assertTrue(EventRepository.allEvents.any { it.title == "Simpósio E2E" })

        // Rola a LazyColumn até o cartão do evento
        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Simpósio E2E") and !hasSetTextAction())

        // Clica no cartão do evento
        composeTestRule.onNode(hasText("Simpósio E2E") and !hasSetTextAction())
            .performClick()

        // Valida que a tela de detalhes abriu
        composeTestRule.onNodeWithText("Detalhes do Evento")
            .assertExists()
    }
}
