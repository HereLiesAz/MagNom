package com.hereliesaz.magnom.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hereliesaz.magnom.data.DeviceRepository
import com.hereliesaz.magnom.services.UsbCommunicationService

/**
 * Factory for creating [TransmissionInterfaceViewModel] instances.
 */
class TransmissionInterfaceViewModelFactory(
    private val application: Application,
    private val deviceRepository: DeviceRepository,
    private val usbCommunicationService: UsbCommunicationService,
    private val cardId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransmissionInterfaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransmissionInterfaceViewModel(application, deviceRepository, usbCommunicationService, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
