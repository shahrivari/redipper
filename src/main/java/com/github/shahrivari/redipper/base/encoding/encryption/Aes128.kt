package com.github.shahrivari.redipper.base.encoding.encryption

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class Aes128(key: ByteArray) {
    private val absEncryptionAlgorithm = "AES"
    private val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    private val secretKey = SecretKeySpec(key, absEncryptionAlgorithm)
    private val ivParameterSpec = IvParameterSpec(key)

    init {
        require(key.size == KEY_LEN) { "AES-128 key length must be 16 bytes but it is: ${key.size}" }
    }

    companion object {
        const val KEY_LEN = 16
    }

    fun encrypt(data: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        return cipher.doFinal(data)
    }
}
