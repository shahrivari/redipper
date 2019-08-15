package com.github.shahrivari.redipper.base.encoding.encryption

import com.github.shahrivari.redipper.base.encoding.Encoder
import kotlin.random.Random

class AesEmbeddedEncoder : Encoder {
    override fun encode(bytes: ByteArray): ByteArray {
        val key = Random.nextBytes(Aes128.KEY_LEN)
        val aes128 = Aes128(key)
        val encrypt = aes128.encrypt(bytes)
        return key + encrypt
    }

    override fun decode(bytes: ByteArray): ByteArray {
        val key = bytes.copyOfRange(0, Aes128.KEY_LEN)
        val data = bytes.copyOfRange(Aes128.KEY_LEN, bytes.size)
        val aes128 = Aes128(key)
        return aes128.decrypt(data)
    }
}