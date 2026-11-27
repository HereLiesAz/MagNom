## Workflow

The project was re-envisioned from the ground up as a Kotlin Multiplatform / Compose Multiplatform
app. Phase status:

1. **Toolchain → KMP + Compose Multiplatform (latest stable):** Done. Gradle 9.7.1, AGP 9.3.1,
   Kotlin 2.3.21, Compose Multiplatform 1.11.1, JDK 21. Android + desktop targets.
2. **Shared core (logic layer):** Done. Correct ISO 7811/7813 codec, F2F, swipe decoder, WAV — in
   `commonMain`, verified by `commonTest` against real vectors.
3. **Typed encrypted data layer:** Done. `Card` invariant, `SecureStore`, kotlinx.serialization.
4. **Transports:** Done (audio common; Bluetooth SPP + USB on Android; the serial protocol is unverified against firmware). All go through one `Transmitter`
   seam that only accepts a validated `Card`.
5. **UI + security:** Done. Six destinations (Cards, Editor, Analyzer, Transmit, Settings, Help) in an AzNavRail shell, Koin DI, biometric app-lock, `FLAG_SECURE`, consent gate.
6. **Hardware end-to-end testing:** Pending physical MagSpoof-class peripheral firmware for the
   Bluetooth SPP / USB serial line protocol.

The historical MagSpoof blueprint and hardware guides in this folder are retained for reference.
