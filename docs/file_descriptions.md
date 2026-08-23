## File Descriptions

MagNom is a single Kotlin Multiplatform / Compose Multiplatform module (`:app`) with
platform-split source sets:

*   `app/src/commonMain/kotlin/com/hereliesaz/magnom/`
    *   `core/` — pure ISO 7811/7813 logic (LRC, TrackCodec, F2F, WAV, SwipeDecoder), no platform deps.
    *   `domain/` — `Card` and its invariant, repository and `Transmitter` interfaces, `expect` declarations.
    *   `data/` — encrypted, typed store over `SecureStore` (kotlinx.serialization).
    *   `transport/` — common `AudioTransmitter` and the `expect AudioSink`.
    *   `ui/` — Compose Multiplatform screens, theme, and ViewModels.
    *   `di/` — the shared Koin graph.
*   `app/src/commonTest/kotlin/...` — standards-anchored core tests.
*   `app/src/androidMain/` — Android actuals (EncryptedSharedPreferences, AudioTrack, BLE/USB
    transmitters), `MainActivity` (biometric app-lock + FLAG_SECURE), `MagNomApp`, manifest, resources.
*   `app/src/desktopMain/` — desktop actuals (AES-GCM file store, javax.sound sink) and `main()`.
*   `docs/` — project documentation (this folder). The MagSpoof blueprint and hardware guides here
    are retained as historical reference.
*   Peripheral firmware for the BLE/USB devices lives in the separate hardware projects, not this repo.
