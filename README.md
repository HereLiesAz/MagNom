# MagNom

MagNom is an Android application designed to serve as a comprehensive tool for researchers, developers, and hobbyists working with magnetic stripe card technology. It provides a user-friendly interface for creating, managing, and securely storing magnetic stripe card profiles. The application is designed to interface with custom hardware peripherals via Bluetooth Low Energy (BLE) to perform magnetic stripe emulation.

## Current Status

The project is currently in the **alpha development stage**. The core infrastructure and several key features of the Android application have been implemented.

### Implemented Features:

*   **Application Architecture:**
    *   Modern MVVM (Model-View-ViewModel) architecture.
    *   UI built with Jetpack Compose.
    *   Type-safe navigation using Jetpack Navigation.
*   **Card Profile Management:**
    *   Create and edit card profiles (name, PAN, expiration, service code).
    *   Securely store card profiles on-device using `EncryptedSharedPreferences` from the Jetpack Security library.
    *   View a list of all saved card profiles.
*   **Magnetic Stripe Logic:**
    *   Business logic for generating valid Track 1 and Track 2 magnetic stripe data strings.
    *   Correct calculation of the Longitudinal Redundancy Check (LRC).
*   **User Interface:**
    *   User feedback on transmission status.
*   **Bluetooth Low Energy (BLE) Communication:**
    *   A foreground service to manage BLE operations.
    *   Scanning for and discovering nearby BLE peripherals.
    *   Connecting to a selected peripheral and managing the connection state.
    *   A reliable queuing mechanism for writing data to the peripheral's characteristics.
    *   Functions to transmit track data and send emulation commands.
*   **Waveform Visualization:**
    *   Display the waveform of the magnetic stripe data.
    *   Allow zooming and panning of the waveform.
    *   Display the corresponding characters under the waveform.
    *   Allow the user to play the sound of the waveform.
*   **Audio File Parsing:**
    *   Allow the user to load an audio file.
    *   Parse the file for magnetic stripe swipe data.
    *   Identify and extract all swipes in the file.
*   **In-App Audio Recording:**
    *   Allow the user to record audio from within the app.
*   **Backup and Restore:**
    *   Allow the user to back up all their data to a password-protected file.
    *   Allow the user to restore their data from a backup file.

### Next Steps:

The next major phase of development will focus on end-to-end testing with a hardware peripheral.

### Future Features:

    *   Provide options to select the recording device (USB, headphone jack, Bluetooth, WiFi).
*   **Create a trimmed audio clip for each swipe.**
