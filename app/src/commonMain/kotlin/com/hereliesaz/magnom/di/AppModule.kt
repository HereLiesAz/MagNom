package com.hereliesaz.magnom.di

import com.hereliesaz.magnom.data.DefaultSettingsRepository
import com.hereliesaz.magnom.data.JsonCardRepository
import com.hereliesaz.magnom.domain.CardRepository
import com.hereliesaz.magnom.domain.SettingsRepository
import com.hereliesaz.magnom.domain.Transmitter
import com.hereliesaz.magnom.transport.AudioSink
import com.hereliesaz.magnom.transport.AudioTransmitter
import com.hereliesaz.magnom.ui.CardEditorViewModel
import com.hereliesaz.magnom.ui.CardListViewModel
import com.hereliesaz.magnom.ui.RawAnalyzerViewModel
import com.hereliesaz.magnom.ui.SettingsViewModel
import com.hereliesaz.magnom.ui.TransmitViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Shared dependency graph. Platform modules add a [com.hereliesaz.magnom.domain.SecureStore],
 * transport-specific [Transmitter]s (BLE/USB on Android) and the audio sink implementation.
 */
val appModule = module {
    single { Json { ignoreUnknownKeys = true; encodeDefaults = true } }

    single<CardRepository> { JsonCardRepository(get(), get()) }
    single<SettingsRepository> { DefaultSettingsRepository(get()) }

    single { AudioSink() }
    single { AudioTransmitter(get()) } bind Transmitter::class

    viewModel { CardListViewModel(get()) }
    viewModel { CardEditorViewModel(get()) }
    viewModel { RawAnalyzerViewModel(get()) }
    viewModel { TransmitViewModel(getAll(), get()) }
    viewModel { SettingsViewModel(get(), getAll()) }
}
