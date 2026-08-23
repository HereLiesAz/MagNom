package com.hereliesaz.magnom.data

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based AES-256-GCM for backup files. The key is derived with PBKDF2
 * (HMAC-SHA256, 210k iterations). File layout: [1B version][16B salt][12B iv][ciphertext].
 */
object BackupCrypto {
    private const val VERSION: Byte = 1
    private const val ITERATIONS = 210_000
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val KEY_BITS = 256

    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LEN).also { random.nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(128, iv))
        }
        val ciphertext = cipher.doFinal(plaintext)
        return byteArrayOf(VERSION) + salt + iv + ciphertext
    }

    fun decrypt(blob: ByteArray, password: CharArray): ByteArray {
        require(blob.isNotEmpty() && blob[0] == VERSION) { "Unsupported backup format" }
        val salt = blob.copyOfRange(1, 1 + SALT_LEN)
        val iv = blob.copyOfRange(1 + SALT_LEN, 1 + SALT_LEN + IV_LEN)
        val ciphertext = blob.copyOfRange(1 + SALT_LEN + IV_LEN, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(128, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
}
