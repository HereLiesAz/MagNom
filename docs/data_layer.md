## Data Layer

The data layer is responsible for handling all application data, ensuring that it is managed securely and efficiently.

*   **Repository:** The repository is the single source of truth for the application's data. It abstracts the origins of the data, whether from a local encrypted database, secure preferences, or a remote server. It provides a clean API for the ViewModel to fetch and save card profiles.
*   **Secure Storage:** The storage of magnetic stripe data, particularly for financial cards, is a significant security responsibility. Plaintext storage is unacceptable. The application must use Android's Jetpack Security library, which provides a robust and easy-to-use abstraction over the hardware-backed Android Keystore system. This allows for the creation of encrypted files and encrypted SharedPreferences. All card profiles must be stored in an encrypted format, with the encryption keys managed securely by the Android Keystore, making them difficult to extract even from a rooted device.
