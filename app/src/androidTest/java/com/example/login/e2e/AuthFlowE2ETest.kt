package com.example.login.e2e

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AuthFlowE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<RegisterActivity>()

    private val auth = FirebaseAuth.getInstance()

    @Before
    fun setUp() {
        auth.signOut()
    }

    @After
    fun tearDown() {
        auth.signOut()
    }

    @Test
    fun e2e_userRegistrationFlow_createsAccountAndNavigatesToHome() {
        val uniqueEmail = "e2e_user_${UUID.randomUUID().toString().take(8)}@campus.com"
        val password = "Password123"

        composeTestRule.onNodeWithText("Email")
            .performTextInput(uniqueEmail)

        composeTestRule.onNodeWithText("Senha")
            .performTextInput(password)

        composeTestRule.onNodeWithText("Confirmar senha")
            .performTextInput(password)

        composeTestRule.onNodeWithText("Cadastre-se")
            .performClick()

        composeTestRule.waitUntil(10_000) {
            auth.currentUser != null
        }

        assertNotNull(auth.currentUser)
    }
}
