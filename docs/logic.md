# Core Business Logic

This document details the core business logic components of MagNom, responsible for handling magnetic stripe data, audio processing, and cryptographic operations.

## Magnetic Stripe Data Manipulation

The application adheres to ISO/IEC 7811 and 7813 standards for magnetic stripe data.

### `LrcCalculator`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/logic/LrcCalculator.kt`

Calculates the Longitudinal Redundancy Check (LRC) character. The LRC is a validity check byte appended to the end of the track data. It is calculated by XORing all the characters in the data message (excluding the start sentinel but including the end sentinel).

### `TrackDataGenerator`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/logic/TrackDataGenerator.kt`

Responsible for formatting raw card data (PAN, Name, Expiration, Service Code) into valid Track 1 and Track 2 strings.
- **Track 1 (IATA):** Format B, 7-bit encoding (6 data bits + 1 parity).
  - Format: `%B[PAN]^[NAME]^[EXP][SVC][DISC]?[LRC]`
- **Track 2 (ABA):** 5-bit encoding (4 data bits + 1 parity).
  - Format: `;[PAN]=[EXP][SVC][DISC]?[LRC]`

### `TrackDataParser`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/logic/TrackDataParser.kt`

Parses full track strings back into their constituent components (PAN, Name, Expiration, etc.). It handles error checking and validation of sentinels and field separators.

### `BinaryDecoder`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/logic/BinaryDecoder.kt`

A low-level utility that decodes binary strings into ASCII characters based on magnetic stripe encoding standards.
- Supports 5-bit (Track 2/3) decoding.
- Supports 7-bit (Track 1) decoding.
- Validates parity bits for each character.

## Audio Processing (F2F Encoding/Decoding)

MagNom includes a sophisticated audio processing engine to interact with audio-jack based readers and to analyze recorded swipes.

### `AudioDecoder`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/audio/AudioDecoder.kt`

Implements the "Aiken Biphase" (F2F) decoding algorithm.
- **Peak Detection:** Uses smart peak detection (and derivative-based fallback) to identify flux transitions in the audio waveform.
- **Bit Extraction:** Measures the distance between peaks to distinguish between '0' (long gap) and '1' (two short gaps).
- **Direction Agnostic:** Attempts to decode in both forward and reverse directions to handle swipes performed in either direction.

### `WaveformDataGenerator`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/logic/WaveformDataGenerator.kt`

Converts digital track data into an audio waveform (PCM data) using F2F encoding. This allows the phone to "play" the magnetic stripe data into a reader head via the audio jack or a coil.
- Generates square waves representing the magnetic flux transitions.
- Supports configurable sample rates and frequencies.

### `AudioPlayer`
**Location:** `app/src/main/kotlin/com/hereliesaz/magnom/logic/AudioPlayer.kt`

Manages the playback of the generated waveforms using the Android `AudioTrack` API.

## Cryptography and Validation

### `LrcCalculator` (Validation)
Used not just for generation but also to validate the integrity of parsed or decoded data.

### Security
While not a single "logic" class, the application uses `EncryptedSharedPreferences` (see Data Layer) to ensure all data handled by these logic classes is stored securely when at rest.
