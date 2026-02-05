## Screens

The application includes the following screens, designed to provide a comprehensive workflow for magnetic stripe analysis and emulation:

*   **Main Screen:** The landing screen that displays a list of all saved card profiles. It serves as the central hub for selecting a card to work with.
*   **Card Editor Screen:** A user-friendly interface for creating or editing card profiles, allowing modification of standard fields like PAN, Name, and Expiration Date.
*   **Advanced Editor Screen:** A more powerful editor that provides direct access to the `CardProfile` data, allowing for deeper modifications.
*   **Advanced Raw Data Editor:** A specialized editor for manipulating the raw binary or hex data of the magnetic tracks, intended for advanced users who need low-level control.
*   **Transmission Interface:** The command center for emulation. It allows the user to select a device (BLE or USB) and transmit the selected card's data to the hardware for spoofing.
*   **Settings Screen:** Provides configuration options for the application, including hardware connection management, security settings, and backup/restore functionality.
*   **Bruteforce Screen:** A utility for performing automated attacks or testing by sequentially generating and transmitting variations of card data (e.g., iterating through CVVs or expiration dates).
*   **Device Screen:** A management screen for discovering, pairing, and connecting to external hardware peripherals via Bluetooth Low Energy (BLE) or USB.
*   **Help Screen:** A context-aware help system that provides detailed instructions and information relevant to the screen the user was previously viewing.
*   **Magspoof Replay Screen:** Dedicated to replaying specific magnetic stripe waveforms or data sequences, often used for testing hardware responsiveness.
*   **Parse Screen:** An audio analysis tool that allows users to visualize audio waveforms (from files or recordings), detect peaks, and decode magnetic stripe data encoded in the audio (F2F decoding).
*   **Swipe Selection Screen:** Used after parsing audio; it presents a list of detected "swipes" (segments of valid data) for the user to choose from.
*   **Create Card Profile Screen:** A flow for finalizing a new card profile based on data extracted from a parsed audio swipe or imported data.
*   **Card Selection Screen:** A modal or intermediate screen allowing the user to pick a card profile for a specific action (like transmission) if one wasn't already selected.
