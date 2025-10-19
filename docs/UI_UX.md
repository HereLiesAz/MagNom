## Section 3.2: User Interface (UI) and User Experience (UX) Design

The application's UI should be clean, intuitive, and focused on its core tasks: managing cards and initiating emulation.

*   **Main Screen:** A primary screen should display a list of saved card profiles using a RecyclerView or a LazyColumn in Compose. Each item should have a user-defined nickname (e.g., "Personal Debit," "Office Access Key," "Hotel Room 123") for easy identification. Tapping an item could lead to a detail/transmit screen.
*   **Card Editor Screen:** This screen is for adding a new card or editing an existing one. It should feature clearly labeled input fields for:
    *   Card Nickname (for user reference)
    *   Primary Account Number (PAN)
    *   Cardholder Name
    *   Expiration Date (using a YY/MM format picker)
    *   Service Code The UI should perform real-time input validation, such as ensuring the PAN contains only numbers and is within the valid length.
*   **Advanced Raw Data Editor:** To cater to researchers and developers, a separate editor should be available. This screen would provide two text fields for the user to paste or manually type the full, raw data strings for Track 1 and Track 2. As the user types, the application should use its parsing logic (from MagstripeDecoder) to decode the string in real-time, displaying the extracted components (PAN, Name, etc.) and validating the format, parity, and LRC. This provides immediate feedback and serves as a powerful analysis tool.
*   **Transmission Interface:** After a card is selected, the UI should present a clear and unambiguous way to initiate the spoofing process. This could be a large, prominent button labeled "Transmit" or "Emulate Swipe." During transmission, the UI must provide clear visual feedback, such as a progress indicator, an animation, or status text like "Transmitting..." and "Ready to Use."
*   **Settings Screen:** A standard settings screen should provide controls for managing the hardware connection (e.g., a button to scan for and pair with the BLE peripheral), application security (e.g., enabling a PIN or biometric lock), and data management options (e.g., backup/restore of card profiles).
