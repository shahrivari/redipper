package com.github.shahrivari.redipper.base.encoding.encryption

import com.github.shahrivari.redipper.base.encoding.Encoder
import java.security.MessageDigest

class AesEncoder(key: String) : Encoder {
    private val aes128: Aes128

    init {
        val md5 = MessageDigest.getInstance("MD5")
        aes128 = Aes128(md5.digest(key.toByteArray()))
    }

    override fun encode(bytes: ByteArray): ByteArray {
        return aes128.encrypt(bytes)
    }

    override fun decode(bytes: ByteArray): ByteArray {
        return aes128.decrypt(bytes)
    }
}