package com.hereliesaz.magnom

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.hereliesaz.magnom.domain.SettingsRepository
import org.koin.android.ext.android.inject

/**
 * Android entry point. Two security guarantees the previous build never had:
 *  - FLAG_SECURE keeps PANs and card photos out of screenshots and the recents thumbnail.
 *  - When the app-lock is enabled, a biometric/device-credential prompt gates the UI.
 */
class MainActivity : FragmentActivity() {

    private val settings: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()

        setContent {
            var unlocked by remember { mutableStateOf(!settings.appLockEnabled.value) }
            if (unlocked) {
                App()
            } else {
                LockScreen()
                promptUnlock { unlocked = true }
            }
        }
    }

    private fun promptUnlock(onUnlocked: () -> Unit) {
        val canAuth = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) { onUnlocked(); return }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onUnlocked()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { finish() }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MagNom")
                .setSubtitle("Authenticate to access stored cards")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                )
                .build(),
        )
    }
}

@androidx.compose.runtime.Composable
private fun LockScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize()) {} }
    }
}
