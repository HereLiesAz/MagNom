package com.hereliesaz.magnom.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.hereliesaz.magnom.ui.screens.ConsentGate
import com.hereliesaz.magnom.ui.screens.HelpScreen
import com.hereliesaz.magnom.ui.theme.MagNomTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Compose Multiplatform UI tests. These execute on the desktop (JVM) target — no device or
 * emulator required — so the UI is actually exercised in CI, not just compiled.
 */
@OptIn(ExperimentalTestApi::class)
class ScreensUiTest {

    @Test
    fun help_screen_lists_the_topics() = runComposeUiTest {
        setContent { MagNomTheme { HelpScreen() } }
        onNodeWithText("Help").assertIsDisplayed()
        onNodeWithText("Cards").assertIsDisplayed()
        onNodeWithText("Transmit").assertIsDisplayed()
    }

    @Test
    fun consent_gate_accept_button_invokes_callback() = runComposeUiTest {
        var accepted = false
        setContent { MagNomTheme { ConsentGate(onAccept = { accepted = true }) } }
        onNodeWithText("I understand and accept").performClick()
        assertTrue(accepted, "Accepting consent should invoke the callback")
    }
}
