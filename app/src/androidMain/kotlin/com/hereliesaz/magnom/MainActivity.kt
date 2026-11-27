package com.hereliesaz.magnom

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.hereliesaz.magnom.domain.SettingsRepository
import com.hereliesaz.magnom.transport.PermissionGate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Android entry point. Security guarantees:
 *  - FLAG_SECURE keeps PANs and card photos out of screenshots and the recents thumbnail.
 *  - When the app-lock is enabled, a biometric/device-credential prompt gates the UI, and the
 *    app locks again every time it leaves the foreground.
 *
 * It also brokers runtime permission requests for transports (see [PermissionGate]).
 */
class MainActivity : FragmentActivity() {

    private val settings: SettingsRepository by inject()

    private var unlocked by mutableStateOf(false)
    private var promptShowing = false
    private var pendingPermission: CompletableDeferred<Boolean>? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            pendingPermission?.complete(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()

        unlocked = !settings.appLockEnabled.value
        PermissionGate.requester = { permission ->
            val answer = CompletableDeferred<Boolean>()
            pendingPermission = answer
            withContext(Dispatchers.Main.immediate) { permissionLauncher.launch(permission) }
            answer.await()
        }

        setContent {
            if (unlocked) {
                App()
            } else {
                LockScreen()
                // Side effect, so it runs once per lock rather than on every recomposition.
                LaunchedEffect(Unit) { promptUnlock() }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Re-lock whenever the app leaves the foreground (but not for rotation-style changes).
        if (!isChangingConfigurations && settings.appLockEnabled.value) unlocked = false
    }

    override fun onDestroy() {
        PermissionGate.requester = null
        super.onDestroy()
    }

    private fun promptUnlock() {
        if (promptShowing) return
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        when (BiometricManager.from(this).canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            // Nothing to authenticate with (no screen lock set up): the lock can't be enforced, so
            // open rather than lock the user out of their own data.
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> { unlocked = true; return }
            // Authenticators exist but can't be used right now. Stay locked; never fail open.
            else -> { finish(); return }
        }

        promptShowing = true
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    promptShowing = false
                    unlocked = true
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    promptShowing = false
                    finish()
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MagNom")
                .setSubtitle("Authenticate to access stored cards")
                .setAllowedAuthenticators(authenticators)
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
