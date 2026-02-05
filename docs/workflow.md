## Workflow

The project tracks its progress through the following phases:

1.  **Phase 1: Environment and Toolchain Setup:**
    *   **Status: Done**
    *   Install the Android development environment.
    *   Initialize version control.

2.  **Phase 2: Custom Peripheral Hardware Assembly and Firmware Deployment:**
    *   **Status: Ongoing (External)**
    *   Select and prototype the hardware components.
    *   Develop the BLE stack and GATT server firmware.
    *   Develop the core emulation logic firmware.

3.  **Phase 3: Android Application Scaffolding and UI Implementation:**
    *   **Status: Done**
    *   Create the Android project.
    *   Define permissions and navigation graph (`AzNavRail`).
    *   Implement the core UI screens and ViewModels.

4.  **Phase 4: Core Logic and Data Management:**
    *   **Status: Done**
    *   Implement magnetic stripe logic (`LrcCalculator`, `TrackDataGenerator`).
    *   Implement secure storage (`EncryptedSharedPreferences`).
    *   Implement backup and restore (`BackupManager`).

5.  **Phase 5: Communication Services (BLE & USB):**
    *   **Status: Done**
    *   Implement `BleCommunicationService` for wireless peripherals.
    *   Implement `UsbCommunicationService` for wired serial connections.
    *   Create unified interfaces for transmission.

6.  **Phase 6: Audio Processing & Analysis Features:**
    *   **Status: Done**
    *   Implement `AudioDecoder` (F2F decoding).
    *   Implement `WaveformDataGenerator` (F2F encoding).
    *   Create Audio Parsing and Swipe Selection screens.
    *   Implement Audio Recording functionality.

7.  **Phase 7: Advanced Features:**
    *   **Status: Done**
    *   Implement `BruteforceScreen`.
    *   Implement `MagspoofReplayScreen`.
    *   Implement OCR (`ImageProcessingRepository`).
    *   Implement Analytics.

8.  **Phase 8: End-to-End System Testing and Debugging:**
    *   **Status: In Progress**
    *   Test with physical hardware.
    *   Verify edge cases (noise, reverse swipes).
    *   Refine UI/UX based on usage.

9.  **Phase 9: Production Build and Packaging:**
    *   **Status: Not Started**
    *   Finalize the application signing.
    *   Prepare release builds.
