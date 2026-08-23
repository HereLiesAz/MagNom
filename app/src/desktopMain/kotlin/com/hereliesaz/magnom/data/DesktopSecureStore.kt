package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.domain.SecureStore
import java.io.File
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop SecureStore: values are AES-256-GCM encrypted with a per-user key stored in the
 * user's home directory. No card data is written in plaintext.
 */
class DesktopSecureStore(
    dir: File = File(System.getProperty("user.home"), ".magnom"),
) : SecureStore {

    private val storeFile = File(dir, "store.properties")
    private val keyFile = File(dir, "key")
    private val key: SecretKeySpec

    init {
        dir.mkdirs()
        key = if (keyFile.exists()) {
            SecretKeySpec(Base64.getDecoder().decode(keyFile.readText().trim()), "AES")
        } else {
            val generated = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            keyFile.writeText(Base64.getEncoder().encodeToString(generated.encoded))
            runCatching { keyFile.setReadable(false, false); keyFile.setReadable(true, true) }
            SecretKeySpec(generated.encoded, "AES")
        }
    }

    override fun readText(key: String): String? {
        val enc = load().getProperty(key) ?: return null
        return runCatching {
            val raw = Base64.getDecoder().decode(enc)
            val iv = raw.copyOfRange(0, 12)
            val cipherText = raw.copyOfRange(12, raw.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, this.key, GCMParameterSpec(128, iv))
            cipher.doFinal(cipherText).decodeToString()
        }.getOrNull()
    }

    override fun writeText(key: String, value: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, this.key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(value.encodeToByteArray())
        val packed = Base64.getEncoder().encodeToString(iv + cipherText)
        val props = load()
        props.setProperty(key, packed)
        storeFile.outputStream().use { props.store(it, "MagNom") }
    }

    override fun remove(key: String) {
        val props = load()
        props.remove(key)
        storeFile.outputStream().use { props.store(it, "MagNom") }
    }

    private fun load(): Properties = Properties().apply {
        if (storeFile.exists()) storeFile.inputStream().use { load(it) }
    }
}
