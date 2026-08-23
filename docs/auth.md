## Authentication &amp; Security

Implemented (Android):

*   **App lock:** When enabled in Settings, a biometric / device-credential prompt
    (`BiometricPrompt`) gates the app before any card is shown.
*   **Screenshot protection:** `FLAG_SECURE` is set on the window, keeping PANs and card data out
    of screenshots and the recents thumbnail.
*   **Encrypted at rest:** All card data is stored via `SecureStore` (EncryptedSharedPreferences
    on Android, AES-256-GCM file on desktop).
*   **No network:** MagNom transmits nothing to any server. The previous analytics path is gone.
*   **Consent gate:** A first-run ethical-use notice blocks the app until acknowledged.
