package com.hereliesaz.magnom.logic

import org.junit.Assert.*
import org.junit.Test

class BinaryDecoderTest {

    @Test
    fun `test decode Track 2`() {
        val binary = "000000" +
                     "11010" + // ;
                     "10000" + // 1
                     "11111" + // ?
                     "10101" + // LRC (10 -> 1010 + 1 parity -> 10101)
                     "00000"

        try {
            val result = BinaryDecoder.decode(binary)
            assertEquals(";1?", result)
        } catch (e: Exception) {
            fail("Exception thrown: ${e.message}")
        }
    }
}
