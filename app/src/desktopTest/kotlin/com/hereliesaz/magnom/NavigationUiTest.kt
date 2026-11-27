package com.hereliesaz.magnom

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.hereliesaz.magnom.di.appModule
import com.hereliesaz.magnom.di.desktopModule
import com.hereliesaz.magnom.domain.SettingsRepository
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * End-to-end walk of every destination through the AzNavRail shell, guarding against the rail
 * swap (or any later shell change) dropping a route. Runs against a throwaway user.home so the
 * real ~/.magnom store is never touched.
 */
@OptIn(ExperimentalTestApi::class)
class NavigationUiTest {
    private var savedHome: String? = null

    @BeforeTest
    fun setUp() {
        savedHome = System.getProperty("user.home")
        System.setProperty("user.home", Files.createTempDirectory("magnom-ui").toString())
        startKoin { modules(appModule, desktopModule) }
        KoinPlatform.getKoin().get<SettingsRepository>().setConsentAccepted(true)
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        savedHome?.let { System.setProperty("user.home", it) }
    }

    @Test
    fun every_destination_is_reachable() = runComposeUiTest {
        setContent { App() }

        // Rail destinations.
        onNodeWithText("New card").assertExists()
        onNodeWithText("Analyze").performClick()
        onNodeWithText("Track 1").assertExists()
        onAllNodesWithText("Settings")[0].performClick()
        onNodeWithText("Backup & restore").assertExists()
        onAllNodesWithText("Cards")[0].performClick()

        // Cards -> Editor -> save -> list.
        onNodeWithText("New card").performClick()
        onNodeWithText("New Card").assertExists() // host title follows the detail route
        onNodeWithText("Label").performTextInput("Test")
        onNodeWithText("PAN (card number)").performTextInput("4111111111111111")
        onNodeWithText("Cardholder name").performTextInput("DOE/JOHN")
        onNodeWithText("Expiration (YYMM)").performTextInput("3012")
        onNodeWithText("Service code (3 digits)").performTextInput("101")
        // Off-screen in the test window, so invoke the semantic action rather than a pointer click.
        onNodeWithText("Save card").performSemanticsAction(SemanticsActions.OnClick)
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Edit").fetchSemanticsNodes().isNotEmpty() }

        // List -> Transmit -> Back.
        onAllNodesWithText("Transmit")[0].performClick()
        onNodeWithText("Emulate swipe").assertExists()
        onNodeWithText("Back").performClick()

        // List -> Edit -> Cancel.
        onNodeWithText("Edit").performClick()
        onNodeWithText("Edit Card").assertExists()
        onNodeWithText("Cancel").performSemanticsAction(SemanticsActions.OnClick)
        onNodeWithText("New card").assertExists()

        // Help last: its body repeats rail labels ("Cards", "Transmit").
        onAllNodesWithText("Help")[0].performClick()
        onNodeWithText("Security & backups").assertExists()
    }
}
