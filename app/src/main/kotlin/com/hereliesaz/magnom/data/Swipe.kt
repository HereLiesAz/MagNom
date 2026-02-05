package com.hereliesaz.magnom.data

/**
 * Represents a detected swipe within an audio recording.
 *
 * A swipe is defined by a start and end index in the audio sample array,
 * corresponding to the region where magnetic flux transitions were detected.
 *
 * @property start The index of the first sample of the swipe.
 * @property end The index of the last sample of the swipe.
 */
data class Swipe(val start: Int, val end: Int)
