## Workflow

The project was re-envisioned from the ground up as a Kotlin Multiplatform / Compose Multiplatform
app. Phase status:

1. **Toolchain → KMP + Compose Multiplatform (latest stable):** Done. Gradle 9.7.1, AGP 9.3.1,
   Kotlin 2.3.21, Compose Multiplatform 1.11.1, JDK 21. Android + desktop targets.
2. **Shared core (logic layer):** Done. Correct ISO 7811/7813 codec, F2F, swipe decoder, WAV — in
   `commonMain`, verified by `commonTest` against real vectors.
3. **Typed encrypted data layer:** Done. `Card` invariant, `SecureStore`, kotlinx.serialization.
4. **Transports:** Done (audio common; BLE + USB on Android). All go through one `Transmitter`
   seam that only accepts a validated `Card`.
5. **UI + security:** Done. Five screens, Koin DI, biometric app-lock, `FLAG_SECURE`, consent gate.
6. **Hardware end-to-end testing:** Pending physical MagSpoof-class peripheral firmware for the
   BLE GATT service / USB serial protocol.

The historical MagSpoof blueprint and hardware guides in this folder are retained for reference.
