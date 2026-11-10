package com.purelymail.admin

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.purelymail.admin.data.repository.PurelymailRepository
import com.purelymail.admin.data.repository.TokenManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainAppTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tokenManager = TokenManager(context)
        tokenManager.setDemoMode(true)
        val repository = PurelymailRepository(tokenManager)

        composeTestRule.setContent {
            MainApp(repository = repository)
        }
    }

    @Test
    fun overviewScreen_isDisplayed() {
        composeTestRule.onNodeWithText("PurelyAdmin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Credit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mailboxes").assertIsDisplayed()
    }
}
