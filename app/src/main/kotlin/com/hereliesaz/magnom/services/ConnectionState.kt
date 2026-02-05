package com.hereliesaz.magnom.services

/**
 * Enumeration representing the connection status of a hardware device.
 */
enum class ConnectionState {
    /** The device is not connected. */
    DISCONNECTED,
    /** The app is attempting to establish a connection. */
    CONNECTING,
    /** The connection is active and ready for communication. */
    CONNECTED
}
