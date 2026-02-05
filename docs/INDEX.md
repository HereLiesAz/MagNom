# MagNom Documentation Library

Welcome to the comprehensive documentation for **MagNom**, an Android application for magnetic stripe analysis, emulation, and research.

## Table of Contents

### 1. General Overview
*   **[README](../README.md):** The project's main entry point, listing features, setup instructions, and current status.
*   **[Workflow](workflow.md):** A roadmap of the project's development phases and their completion status.
*   **[Conduct](conduct.md):** Ethical guidelines and legal warnings regarding the use of this software.

### 2. Architecture & Design
*   **[Screens](screens.md):** A detailed list of all application screens and their purposes.
*   **[Data Layer](data_layer.md):** Explains how data is stored (EncryptedSharedPreferences), managed (Repositories), and backed up.
*   **[Core Logic](logic.md):** Details the business logic for magnetic stripe data (LRC, Track 1/2) and audio processing (F2F decoding/encoding).
*   **[Task Flow](task_flow.md):** Describes the typical user journey for key operations like Emulation.

### 3. Technical Guides
*   **[Testing](testing.md):** Procedures for verifying the application with hardware and unit tests.
*   **[Performance](performance.md):** Considerations for efficient architecture and communication.
*   **[Authentication](auth.md):** Details on app security and access control.
*   **[Faux Pas](fauxpas.md):** Critical mistakes to avoid during development (e.g., plaintext storage).

### 4. Hardware Integration
*   **[Magnetic Stripe Replay Device Guide](Magnetic%20Stripe%20Replay%20Device%20Guide.md):** Documentation for the custom hardware peripherals.
*   **[Android Magspoof v4 USB Control](Android%20Magspoof%20v4%20USB%20Control.md):** Specific protocol details for USB communication.

### 5. Miscellaneous
*   **[File Descriptions](file_descriptions.md):** High-level directory structure overview.
*   **[Misc](misc.md):** Feasibility analyses and other notes.
