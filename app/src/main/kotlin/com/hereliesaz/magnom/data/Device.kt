package com.hereliesaz.magnom.data

/**
 * Represents a supported hardware device for magnetic emulation.
 *
 * @property id Unique internal identifier for the device type.
 * @property name Human-readable name of the device.
 * @property description A brief description of the device's capabilities.
 * @property imageUrl URL or resource identifier for the device's image.
 * @property links A list of external links (documentation, store) associated with the device.
 * @property type The protocol type used by the device (e.g., USB, BLE).
 * @property isPinned Whether the user has pinned this device for quick access.
 */
data class Device(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val links: List<Pair<String, String>>,
    val type: DeviceType,
    var isPinned: Boolean = false
)

/**
 * Enumeration of supported device connection protocols/types.
 */
enum class DeviceType {
    /** Connected via USB Serial (CDC). */
    USB_SERIAL,
    /** Connected via Bluetooth Low Energy. */
    BLE,
    /** Specific module for Flipper Zero (GPIO/UART). */
    FLIPPER_MODULE,
    /** Generic reader/writer hardware. */
    READER_WRITER,
    /** Supports multiple connection methods. */
    MULTI_PROTOCOL
}
