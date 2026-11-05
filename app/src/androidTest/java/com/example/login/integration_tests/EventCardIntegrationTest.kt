package com.example.login

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.data.model.Event
import com.example.login.data.repository.EventRepository
import com.example.login.ui.components.EventCard
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCardIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleEvent = Event(
        id = 99,
        title = "Feira de Carreiras Integrada",
        description = "Descrição teste",
        date = "30 de Out, 10:00",
        location = "Bloco B",
        organizer = "DA",
        category = "Carreira"
    )

    @Before
    fun setUp() {
        EventRepository.clearLocalData()
    }

    @Test
    fun eventCard_displaysTitleAndCategory_andHandlesClicks() {
        var clicked = false

        composeTestRule.setContent {
            EventCard(
                event = sampleEvent,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("FEIRA DE CARREIRAS INTEGRADA").assertDoesNotExist()
        composeTestRule.onNodeWithText("Feira de Carreiras Integrada").performClick()

        Assert.assertTrue(clicked)
    }

    @Test
    fun eventCard_favoriteButton_togglesFavoriteInRepository() {
        composeTestRule.setContent {
            EventCard(
                event = sampleEvent,
                onClick = {}
            )
        }

        Assert.assertFalse(EventRepository.isFavorite(99))

        composeTestRule.onNodeWithContentDescription("Favoritar evento").performClick()

        Assert.assertTrue(EventRepository.isFavorite(99))
    }
}
