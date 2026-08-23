## Data Layer

All persistence is typed and encrypted, and nothing leaves the device.

### `Card` (domain)
The core data model. Its constructor is private; the only ways to create one are
`Card.fromFields(...)` (generates the tracks) and `Card.fromRawTracks(...)` (parses and validates
supplied tracks). Both return a failure rather than a half-populated object, so **a Card can never
exist without valid, LRC-checked Track 1 and Track 2 strings.** This is the structural fix for the
old "save a card, then transmit empty bytes over BLE" bug.

### `SecureStore`
A small encrypted key/value interface. Android implements it with EncryptedSharedPreferences
(AES-256, Keystore-bound); desktop implements it with an AES-256-GCM file keyed per user. No card
data is ever written in plaintext.

### `CardRepository` / `JsonCardRepository`
Persists the observable list of cards as kotlinx.serialization JSON inside the `SecureStore`.
Because every `Card` is already validated, the store never has to defend against empty tracks.

### `SettingsRepository`
On-device settings only: consent acceptance and the app-lock toggle.

### Removed
The network `AnalyticsRepository` (which POSTed card structure to a server), the Gson blob store,
Ktor, and the OCR/ImageProcessing repository were all removed.
