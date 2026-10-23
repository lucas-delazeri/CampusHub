package com.example.login

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.ui.screens.CreateEventScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateEventScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun createEventScreen_fillsFormAndSubmitsSuccessfully() {
        var submittedTitle = ""
        var submittedCategory = ""
        var submittedDate = ""
        var submittedLocation = ""
        var submittedDescription = ""
        var submittedLat: Double? = 0.0
        var submittedLng: Double? = 0.0

        composeTestRule.setContent {
            CreateEventScreen(
                onBack = {},
                onCreateEvent = { title, description, date, location, category, lat, lng ->
                    submittedTitle = title
                    submittedDescription = description
                    submittedDate = date
                    submittedLocation = location
                    submittedCategory = category
                    submittedLat = lat
                    submittedLng = lng
                }
            )
        }

        composeTestRule.onNodeWithText("Título do Evento")
            .performScrollTo()
            .performTextInput("Workshop Kotlin")

        composeTestRule.onNodeWithText("Categoria")
            .performScrollTo()
            .performTextInput("Tecnologia")

        composeTestRule.onNodeWithText("Data e Horário")
            .performScrollTo()
            .performTextInput("25 de Out, 14:00")

        composeTestRule.onNodeWithText("Nome do Local / Endereço")
            .performScrollTo()
            .performTextInput("Auditório A")

        composeTestRule.onNodeWithText("Descrição Completa")
            .performScrollTo()
            .performTextInput("Evento de teste de integração")

        composeTestRule.onNodeWithText("Publicar Evento")
            .performScrollTo()
            .performClick()

        assertEquals("Workshop Kotlin", submittedTitle)
        assertEquals("Tecnologia", submittedCategory)
        assertEquals("25 de Out, 14:00", submittedDate)
        assertEquals("Auditório A", submittedLocation)
        assertEquals("Evento de teste de integração", submittedDescription)
        assertNull(submittedLat)
        assertNull(submittedLng)
    }
}
