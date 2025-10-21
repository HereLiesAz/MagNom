package com.hereliesaz.magnom.data

data class Device(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val links: List<Pair<String, String>>,
    val type: DeviceType,
    var isPinned: Boolean = false
)

enum class DeviceType {
    USB_SERIAL,
    BLE,
    FLIPPER_MODULE,
    READER_WRITER,
    MULTI_PROTOCOL
}
