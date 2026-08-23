# Core Logic (`commonMain/core`)

Pure Kotlin, zero platform dependencies, verified by `commonTest` against hand-derived ISO
vectors. One canonical implementation of each concern is shared by every layer.

### `TrackFormat`
Single source of truth for the ISO/IEC 7811/7813 constants (bits per character, base offset,
sentinels) for Track 1, 2 and 3.

### `Lrc`
One Longitudinal Redundancy Check, computed as even column parity over the *data values* of the
characters from the start sentinel through the end sentinel, re-adding the base offset so the
result is always a legal track character. This replaces the previous raw-ASCII XOR that produced
out-of-range control characters for ~87% of PANs and crashed the waveform generator.

### `TrackCodec`
Generates and parses Track 1 / Track 2 strings, and converts complete track strings to and from
the bit stream (LSB-first data bits + odd parity per character). Encode and decode share `Lrc` and
`TrackFormat`, so a generated string always decodes back to itself.

### `F2f`
F2F (Aiken biphase) modulation: a bit stream to/from PCM. A `0` is one transition per cell; a `1`
adds a mid-cell transition.

### `SwipeDecoder`
Recovers a track string from recorded F2F audio: transition detection → adaptive-clock interval
decoding (a `1` requires a *pair* of short intervals — the fix for the previous "assume validity"
gap) → `TrackCodec`. Tries both swipe directions and both track formats.

### `Wav`
Dependency-free 16-bit PCM mono WAV reader/writer.
