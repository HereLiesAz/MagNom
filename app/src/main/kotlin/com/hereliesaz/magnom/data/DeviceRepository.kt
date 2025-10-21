package com.hereliesaz.magnom.data

class DeviceRepository {

    fun getDevices(): List<Device> {
        return listOf(
            Device(
                id = "magspoof_v4",
                name = "MagSpoof V4",
                description = "An 8-bit CH55x series microcontroller-based device with USB programmability. It supports emulation of all three magnetic stripe tracks.",
                imageUrl = "https://i.imgur.com/8Fk7t6G.png",
                links = listOf(
                    "Getting Started Guide" to "https://www.hackster.io/electronic-cats/magspoof-v4-getting-started-7c97af",
                    "KSEC Labs" to "https://labs.ksec.co.uk/product/magspoof-v4-magnetic-card-emulator-electronic-cats-pre-order/"
                ),
                type = DeviceType.USB_SERIAL
            ),
            Device(
                id = "magspoof_v5",
                name = "MagSpoof V5",
                description = "The latest version featuring a CH552G microcontroller and a USB-C connector for programming and charging.",
                imageUrl = "https://i.imgur.com/GzB28yH.png",
                links = listOf(
                    "Official Store" to "https://electroniccats.com/store/magspoof-v5/",
                    "KSEC Labs" to "https://labs.ksec.co.uk/product/magspoof-v5-latest-magnetic-card-emulator-electronic-cats/"
                ),
                type = DeviceType.USB_SERIAL
            ),
            Device(
                id = "nfc_copy_cat",
                name = "NFC Copy Cat",
                description = "A dual-function device for researching payment systems, combining Near Field Communication (NFC) and magnetic stripe emulation capabilities.",
                imageUrl = "https://i.imgur.com/GzB28yH.png",
                links = listOf(
                    "CircuitPython" to "https://circuitpython.org/board/nfc_copy_cat/",
                    "KSEC Labs" to "https://labs.ksec.co.uk/product/nfc-copy-cat/"
                ),
                type = DeviceType.MULTI_PROTOCOL
            ),
            Device(
                id = "bombercat",
                name = "BomberCat",
                description = "A versatile, all-in-one pentesting platform incorporating NFC, MagSpoof, Wi-Fi, and Bluetooth Low Energy (BLE).",
                imageUrl = "https://i.imgur.com/y35s14J.png",
                links = listOf(
                    "Official Store" to "https://electroniccats.com/store/bombercat/",
                    "Getting Started Guide" to "https://www.hackster.io/electronic-cats/getting-started-bombercat-fe8316"
                ),
                type = DeviceType.MULTI_PROTOCOL
            ),
            Device(
                id = "flipper_magspoof",
                name = "Flipper Zero MagSpoof Module",
                description = "An official add-on module for the Flipper Zero that provides the core MagSpoof circuit (TC4424 driver and coil) adapted for the Flipper's pinout.",
                imageUrl = "https://i.imgur.com/y35s14J.png",
                links = listOf(
                    "Official Store" to "https://electroniccats.com/store/flipper-add-on-magspoof/",
                    "Getting Started Guide" to "https://www.hackster.io/electronic-cats/flipper-add-on-magspoof-getting-started-f79658"
                ),
                type = DeviceType.FLIPPER_MODULE
            )
        )
    }
}
