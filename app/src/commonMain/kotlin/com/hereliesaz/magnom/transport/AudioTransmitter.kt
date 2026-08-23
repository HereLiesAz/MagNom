package com.hereliesaz.magnom.transport

import com.hereliesaz.magnom.core.F2f
import com.hereliesaz.magnom.core.TrackFormat
import com.hereliesaz.magnom.domain.Card
import com.hereliesaz.magnom.domain.TransmitResult
import com.hereliesaz.magnom.domain.Transmitter
import com.hereliesaz.magnom.domain.TransportKind
import com.hereliesaz.magnom.domain.TransportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Transmits a card by generating its Track 2 F2F waveform and playing it through the audio
 * output — the audio-jack / coil path. This transport works on every platform with an audio
 * output, so it is always available and needs no device pairing.
 */
class AudioTransmitter(private val sink: AudioSink) : Transmitter {

    override val kind = TransportKind.AUDIO

    private val _status = MutableStateFlow(TransportStatus.READY)
    override val status: StateFlow<TransportStatus> = _status.asStateFlow()

    private val _target = MutableStateFlow<String?>("Audio output")
    override val target: StateFlow<String?> = _target.asStateFlow()

    override fun isAvailable(): Boolean = true

    override suspend fun transmit(card: Card): TransmitResult {
        return try {
            _status.value = TransportStatus.TRANSMITTING
            val pcm = F2f.trackToPcm(card.track2, TrackFormat.TRACK2)
            sink.play(pcm, F2f.DEFAULT_SAMPLE_RATE)
            _status.value = TransportStatus.READY
            TransmitResult.Success
        } catch (e: Throwable) {
            _status.value = TransportStatus.READY
            TransmitResult.Failure(e.message ?: "Audio playback failed")
        }
    }

    override fun release() {
        sink.stop()
        _status.value = TransportStatus.READY
    }
}
