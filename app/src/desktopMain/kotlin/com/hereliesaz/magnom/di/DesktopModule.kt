package com.hereliesaz.magnom.di

import com.hereliesaz.magnom.data.DesktopSecureStore
import com.hereliesaz.magnom.domain.SecureStore
import com.hereliesaz.magnom.domain.Backups
import com.hereliesaz.magnom.data.DefaultBackups
import java.io.File
import org.koin.dsl.module

/** Desktop bindings. Only the audio transport is available; BLE/USB are Android-only. */
val desktopModule = module {
    single<SecureStore> { DesktopSecureStore() }
    single<Backups> { DefaultBackups(get(), get(), File(System.getProperty("user.home"), ".magnom/backups")) }
}
