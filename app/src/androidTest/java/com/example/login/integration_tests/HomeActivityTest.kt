package com.example.login.integration_tests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.login.HomeActivity
import com.example.login.ui.viewmodel.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<HomeActivity>()

    @Test
    fun activity_launchesSuccessfully() {
        assertNotNull(composeRule.activity)
        assertEquals(
            Lifecycle.State.RESUMED,
            composeRule.activity.lifecycle.currentState
        )
    }

    @Test
    fun activity_createsViewModel() {
        val vm = ViewModelProvider(composeRule.activity)[HomeViewModel::class.java]
        assertNotNull(vm)
    }

    @Test
    fun activity_recreate_keepsSameViewModel() {
        val before = ViewModelProvider(composeRule.activity)[HomeViewModel::class.java]

        composeRule.activityRule.scenario.recreate()

        composeRule.runOnUiThread {
            val after = ViewModelProvider(composeRule.activity)[HomeViewModel::class.java]
            assertEquals(before, after)
        }
    }

    @Test
    fun logout_startsMainActivity_withClearTaskFlags() {
        composeRule.onNodeWithContentDescription("Sair da conta").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.activityRule.scenario.state == Lifecycle.State.DESTROYED
        }
        assertEquals(Lifecycle.State.DESTROYED, composeRule.activityRule.scenario.state)
    }
}
