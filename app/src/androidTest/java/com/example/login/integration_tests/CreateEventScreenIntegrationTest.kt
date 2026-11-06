package com.example.login.integration_tests

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.ui.screens.CreateEventScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        var submittedMaxCapacity = 0
        var imgCode: String? = null
        var isEnded = false

        composeTestRule.setContent {
            CreateEventScreen(
                onBack = {},
                onCreateEvent = { title, description, date, location, category, lat, lng, maxCapacity, imageBase64, is_ended ->
                    submittedTitle = title
                    submittedDescription = description
                    submittedDate = date
                    submittedLocation = location
                    submittedCategory = category
                    submittedLat = lat
                    submittedLng = lng
                    submittedMaxCapacity = maxCapacity
                    imgCode = imageBase64
                    isEnded = is_ended
                }
            )
        }

        val textFields = composeTestRule.onAllNodes(hasSetTextAction())

        textFields[0].performScrollTo().performTextInput("Workshop Kotlin")

        textFields[1].performScrollTo().performTextInput("100")

        textFields[2].performScrollTo().performTextInput("Tecnologia")

        textFields[3].performScrollTo().performTextInput("25 de Out, 14:00")

        textFields[4].performScrollTo().performTextInput("Auditório A")

        textFields[5].performScrollTo().performTextInput("Evento de teste de integração")

        composeTestRule.onNodeWithText("Publicar Evento")
            .performScrollTo()
            .performClick()

        assertEquals("Workshop Kotlin", submittedTitle)
        assertEquals(100, submittedMaxCapacity)
        assertEquals("Tecnologia", submittedCategory)
        assertEquals("25 de Out, 14:00", submittedDate)
        assertEquals("Auditório A", submittedLocation)
        assertEquals("Evento de teste de integração", submittedDescription)
        assertNull(submittedLat)
        assertNull(submittedLng)
        assertNull(imgCode)
        assertFalse(isEnded)
    }
}
