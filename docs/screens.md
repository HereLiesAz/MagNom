## Screens

The re-envisioned app is deliberately scoped to five destinations, matching the original
blueprint's "clean, focused on its core tasks" mandate. All UI is Compose Multiplatform in
`commonMain` and runs identically on Android and desktop.

*   **Card Profiles (list):** The home screen. Lists saved cards; each row transmits, edits, or
    deletes. A card row shows only the masked PAN and expiry.
*   **Card Editor:** Create or edit a card from human-readable fields (label, PAN, name,
    expiration, service code). Track 1 and Track 2 are generated and LRC-checked on save — a card
    cannot be saved with invalid or empty track data.
*   **Raw Data Analyzer:** The blueprint's advanced raw editor, now reachable and with live
    feedback: paste Track 1 / Track 2 and see LRC/format validity and the decoded PAN/expiry in
    real time, then save as a card.
*   **Transmit:** One screen for every transport. Pick Audio (always available), BLE, or USB and
    emulate the swipe. It only ever hands a whole, validated `Card` to the transport.
*   **Settings:** App-lock toggle (biometric / device credential on Android), transport
    availability, and a statement of the on-device, no-network data policy.

A first-run **consent gate** blocks the app until the ethical-use notice is acknowledged.

### Removed from the previous build
Bruteforce, MagspoofReplay, the free-text USB "Send and Spoof", the standalone Device screen, the
separate audio Parse/SwipeSelection/CreateProfile flow, and the duplicate Advanced Editor were all
removed or folded in. Audio swipe decoding now lives in the shared core (`SwipeDecoder`) and feeds
the Raw Analyzer rather than a dead-end flow.
