# MagNom

MagNom is a **Kotlin Multiplatform / Compose Multiplatform** research and developer tool for
analysing, generating, and emulating magnetic stripe data. It runs as an **Android** app and a
**desktop** (JVM) app from one shared codebase, and drives magstripe-emulation hardware over
Bluetooth Low Energy, USB serial, or an F2F audio waveform.

This is the ground-up re-envisioning of the project against its original goals: the phone (or
desktop) is the host "brain"; a strictly separated, pure-Kotlin core owns all the logical layer
(track formatting, LRC, F2F); the UI stays focused on two tasks — manage cards and emulate one;
and all data stays encrypted, on-device.

## Architecture

```
commonMain
  core/      Pure ISO/IEC 7811/7813 logic — LRC, TrackCodec, F2F, WAV, SwipeDecoder (no platform deps)
  domain/    Card (invariant: cannot exist without valid tracks), repositories, Transmitter
  data/      Encrypted, typed store (kotlinx.serialization over an expect SecureStore)
  transport/ AudioTransmitter (F2F over the audio output) + expect AudioSink
  ui/        Compose Multiplatform: 5 screens, theme, ViewModels
  di/        Koin graph
androidMain  EncryptedSharedPreferences store, AudioTrack sink, BLE + USB transmitters,
             biometric app-lock + FLAG_SECURE, Koin/Android entry points
desktopMain  AES-GCM file store, javax.sound sink, desktop entry point
```

### Why this shape

The core logic is the crown jewel, so it lives in `commonMain` with **zero platform
dependencies** and is verified by `commonTest` against hand-derived ISO vectors — not against
itself. Platform capabilities (secure storage, audio output, BLE/USB, biometrics) are
`expect`/`actual` or Koin-provided, so the shared code never reaches for an Android API directly.

## Correctness

The `LrcCalculator` in the previous build XORed raw ASCII codes without removing the track base
offset, producing out-of-range LRC characters for ~87% of real PANs (one of which crashed the
waveform screen), and the test suite asserted the implementation against itself. The rewrite has
a single canonical LRC/codec used by every layer, and the tests assert:

- a hand-computed LRC vector (`;12345?` → `5`);
- that every generated LRC is a legal track character (regression for the crash);
- Track 1/Track 2 generate → parse round-trips;
- encode → decode bit round-trips;
- F2F PCM → `SwipeDecoder` round-trips, forwards **and** reversed;
- WAV encode/decode round-trips;
- rejection of tampered LRCs.

## Security &amp; conduct

- All card data is stored **encrypted at rest** and **never leaves the device** — the network
  analytics path from the previous build was removed entirely.
- An optional **biometric / device-credential app-lock** gates the app (Android).
- `FLAG_SECURE` keeps PANs and card data out of screenshots and the recents thumbnail (Android).
- A **first-run consent gate** actually blocks the app until acknowledged.
- Positioned strictly as a research/developer tool; fraud-oriented features are not included.

## Build

Requires JDK 21 and the Android SDK (compileSdk 37).

```bash
./gradlew :app:desktopTest            # run the core test suite
./gradlew :app:assembleDebug          # build the Android APK
./gradlew :app:run                    # run the desktop app
```

Toolchain: Gradle 9.7.1, AGP 9.3.1, Kotlin 2.3.21, Compose Multiplatform 1.11.1, Koin 4.2.2.

## Status

Alpha. The shared core is complete and tested; the Android and desktop apps build and run. BLE
and USB transports target a custom MagSpoof-class GATT service / serial line protocol and require
matching peripheral firmware; the audio (F2F) transport works on any device with audio output.

## Legal

Unauthorised capture or use of another party's card data is illegal. MagNom is for use with cards
you own or are authorised to use. You are solely responsible for how you use it.
