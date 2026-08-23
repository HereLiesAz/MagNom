package com.hereliesaz.magnom.di

import com.hereliesaz.magnom.data.AndroidSecureStore
import com.hereliesaz.magnom.domain.SecureStore
import com.hereliesaz.magnom.domain.Transmitter
import com.hereliesaz.magnom.domain.Backups
import com.hereliesaz.magnom.data.DefaultBackups
import java.io.File
import com.hereliesaz.magnom.transport.BluetoothSppTransmitter
import com.hereliesaz.magnom.transport.UsbTransmitter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

/** Android-specific bindings: encrypted storage and the BLE/USB transports. */
val androidModule = module {
    single<SecureStore> { AndroidSecureStore(androidContext()) }
    single { BluetoothSppTransmitter(androidContext()) } bind Transmitter::class
    single { UsbTransmitter(androidContext()) } bind Transmitter::class
    single<Backups> { DefaultBackups(get(), get(), File(androidContext().filesDir, "backups")) }
}
