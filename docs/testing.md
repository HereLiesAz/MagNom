## Testing

The following testing procedures should be followed to ensure the quality and reliability of the application:

*   **Set Up Test Environment:**
    *   Obtain a physical magnetic stripe reader and connect it to a PC. Use software (e.g., a simple terminal program for a serial reader, or Notepad for a keyboard wedge reader) to view the output of a swipe.
*   **Perform Integration Testing:**
    *   Use the Android app to connect to the peripheral over Bluetooth SPP or USB serial.
    *   Create a new card profile in the app (e.g., a standard test card format).
    *   Transmit the data to the peripheral. Verify via logs or a debugger that the correct data is sent and received.
    *   Trigger the "emulate" command from the app. Place the peripheral's coil near the test reader's head.
    *   Verify that the test reader correctly decodes and displays the card data.
*   **Test Edge Cases and Debug:**
    *   Test with maximum-length track data.
    *   Test with special characters (on Track 1).
    *   Test the system's response to Bluetooth/USB connection drops and reconnections.
    *   Capture the serial bytes (a USB-serial monitor, or logging on the bridge) to inspect the frames being exchanged and debug any protocol-level issues.
