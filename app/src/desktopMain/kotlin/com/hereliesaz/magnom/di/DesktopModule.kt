package com.hereliesaz.magnom.di

import com.hereliesaz.magnom.data.DesktopSecureStore
import com.hereliesaz.magnom.domain.SecureStore
import org.koin.dsl.module

/** Desktop bindings. Only the audio transport is available; BLE/USB are Android-only. */
val desktopModule = module {
    single<SecureStore> { DesktopSecureStore() }
}
