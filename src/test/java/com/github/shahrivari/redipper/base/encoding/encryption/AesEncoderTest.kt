package com.github.shahrivari.redipper.base.encoding.encryption

import com.github.shahrivari.redipper.util.AppTestUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AesEncoderTest : AppTestUtils {
    @Test
    internal fun `test encode and decode`() {
        val plainString = randomString(17)
        val aes128 = AesEncoder(plainString)

        val encryptStr = aes128.encode(plainString.toByteArray())
        val decryptStr = String(aes128.decode(encryptStr))

        Assertions.assertThat(plainString).isEqualTo(decryptStr)
    }
}