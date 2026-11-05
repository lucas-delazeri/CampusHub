package com.example.login.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.HomeActivity
import com.example.login.data.repository.EventRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EventManagementE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @Before
    fun setUp() {
        val auth = FirebaseAuth.getInstance()
        Tasks.await(
            auth.signInWithEmailAndPassword("profe@gmail.com", "Profe123"),
            15, TimeUnit.SECONDS
        )
        EventRepository.clearLocalData()
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

        val textFields = composeTestRule.onAllNodes(hasSetTextAction())

        textFields[0].performScrollTo().performTextInput("Simpósio E2E")
        textFields[1].performScrollTo().performTextInput("Acadêmico")
        textFields[2].performScrollTo().performTextInput("30 de Nov, 10:00")
        textFields[3].performScrollTo().performTextInput("Bloco Central")
        textFields[4].performScrollTo().performTextInput("Descrição do evento criado via E2E test")

        composeTestRule.runOnUiThread {
            EventRepository.userRole = "professor"
        }

        composeTestRule.onNodeWithText("Publicar Evento")
            .performScrollTo()
            .performClick()

        try {
            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasScrollAction())
                .performScrollToNode(hasText("Simpósio E2E"))

            composeTestRule.onNodeWithText("Simpósio E2E")
                .assertIsDisplayed()
                .performClick()

            composeTestRule.onNodeWithText("Detalhes do Evento")
                .assertExists()
        } catch (e: Throwable) {
            val tela = composeTestRule.onRoot(useUnmergedTree = true).printToString()
            throw AssertionError("Evento não apareceu. Tela atual:\n$tela", e)
        }
        composeTestRule.onNodeWithText("Simpósio E2E")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Detalhes do Evento")
            .assertExists()
    }
}
