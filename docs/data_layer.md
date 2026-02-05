## Data Layer

The data layer is responsible for handling all application data, ensuring that it is managed securely, efficiently, and consistently.

### Repositories

The application uses the Repository pattern to abstract data sources from the UI and logic layers.

*   **`CardRepository`**
    *   **Purpose:** The primary repository for managing user-created `CardProfile` objects.
    *   **Storage:** Uses **EncryptedSharedPreferences** (via Android Jetpack Security) to store card data. Each card is serialized (likely to JSON) and stored securely.
    *   **Functionality:** Provides CRUD (Create, Read, Update, Delete) operations for cards.

*   **`SettingsRepository`**
    *   **Purpose:** Manages application-wide settings such as "Ethical Use" acceptance, default preferences, and theme choices.
    *   **Storage:** Uses a separate `SharedPreferences` instance (potentially encrypted or standard, depending on sensitivity).

*   **`DeviceRepository`**
    *   **Purpose:** Manages the list of known or paired hardware devices (BLE and USB).
    *   **Functionality:** persists known device addresses or identifiers to allow for auto-reconnection.

*   **`AnalyticsRepository`**
    *   **Purpose:** Collects and transmits **anonymized** data to a backend server for research purposes (e.g., character set usage, track lengths).
    *   **Privacy:** Strips all PII (Personally Identifiable Information) like PANs and Names before transmission.
    *   **Endpoint:** Configurable, supports local debug servers.

*   **`ImageProcessingRepository`**
    *   **Purpose:** specialized repository for handling OCR (Optical Character Recognition) tasks.
    *   **Implementation:** Wraps Google ML Kit's Text Recognition to parse card numbers and names from images.

### Backup & Restore

*   **`BackupManager`**
    *   **Purpose:** Handles the export and import of application data.
    *   **Security:** Creates **password-protected ZIP archives** (using `zip4j`) containing the application's shared preferences files. This ensures that backups are just as secure as the on-device storage.

### Data Models

*   **`CardProfile`**: The core data class representing a magnetic stripe card. Contains fields for `track1`, `track2`, `pan`, `expirationDate`, `name`, etc.
*   **`AnonymizedCardProfile`**: A safe version of `CardProfile` used for analytics, containing only structural metadata.
