package com.hereliesaz.magnom.data

import com.hereliesaz.magnom.domain.SecureStore
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
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
        ownerOnly(dir, directory = true)
        key = if (keyFile.exists()) {
            SecretKeySpec(Base64.getDecoder().decode(keyFile.readText().trim()), "AES")
        } else {
            val generated = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            writeAtomically(keyFile, Base64.getEncoder().encodeToString(generated.encoded).encodeToByteArray())
            SecretKeySpec(generated.encoded, "AES")
        }
    }

    /** Restrict to the owner. POSIX permissions where available, java.io flags elsewhere (Windows). */
    private fun ownerOnly(f: File, directory: Boolean = false) {
        val posix = runCatching {
            Files.setPosixFilePermissions(
                f.toPath(),
                PosixFilePermissions.fromString(if (directory) "rwx------" else "rw-------"),
            )
        }.isSuccess
        if (!posix) runCatching {
            f.setReadable(false, false); f.setWritable(false, false)
            f.setReadable(true, true); f.setWritable(true, true)
            if (directory) { f.setExecutable(false, false); f.setExecutable(true, true) }
        }
    }

    /**
     * Writes via a temp file that is owner-only before any content lands in it, then moves it
     * into place, so there is never a world-readable window and a crash mid-write can't
     * truncate the existing file.
     */
    private fun writeAtomically(target: File, bytes: ByteArray) {
        val tmp = File(target.parentFile, target.name + ".tmp")
        tmp.writeBytes(ByteArray(0))
        ownerOnly(tmp)
        tmp.writeBytes(bytes)
        try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun save(props: Properties) {
        val buf = java.io.ByteArrayOutputStream()
        props.store(buf, "MagNom")
        writeAtomically(storeFile, buf.toByteArray())
    }

    override fun readText(key: String): String? {
        val props = load()
        val enc = props.getProperty(key) ?: return null
        return runCatching {
            val raw = Base64.getDecoder().decode(enc)
            val iv = raw.copyOfRange(0, 12)
            val cipherText = raw.copyOfRange(12, raw.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, this.key, GCMParameterSpec(128, iv))
            cipher.doFinal(cipherText).decodeToString()
        }.getOrElse {
            // Undecryptable (wrong key, damaged file). Keep the ciphertext under a side name so a
            // later write to this entry can't destroy data that a restored key could still open.
            val side = "$key.unreadable"
            if (props.getProperty(side) == null) {
                props.setProperty(side, enc)
                runCatching { save(props) }
            }
            null
        }
    }

    override fun writeText(key: String, value: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, this.key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(value.encodeToByteArray())
        val packed = Base64.getEncoder().encodeToString(iv + cipherText)
        val props = load()
        props.setProperty(key, packed)
        save(props)
    }

    override fun remove(key: String) {
        val props = load()
        props.remove(key)
        save(props)
    }

    private fun load(): Properties = Properties().apply {
        if (storeFile.exists()) storeFile.inputStream().use { load(it) }
    }
}
