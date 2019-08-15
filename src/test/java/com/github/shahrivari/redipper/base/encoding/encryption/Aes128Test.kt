package com.github.shahrivari.redipper.base.encoding.encryption

import com.github.shahrivari.redipper.util.AppTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class Aes128Test : AppTestUtils {
    @Test
    internal fun `test encode and decode`() {
        val plainString = randomString(16)
        val aes128 = Aes128(Random.nextBytes(Aes128.KEY_LEN))

        val encryptStr = aes128.encrypt(plainString.toByteArray())
        val decryptStr = String(aes128.decrypt(encryptStr))

        assertThat(plainString).isEqualTo(decryptStr)
    }
}
