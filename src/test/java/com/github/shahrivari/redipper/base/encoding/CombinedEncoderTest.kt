package com.github.shahrivari.redipper.base.encoding

import com.github.shahrivari.redipper.base.encoding.compression.GzipEncoder
import com.github.shahrivari.redipper.base.encoding.compression.Lz4Encoder
import com.github.shahrivari.redipper.base.encoding.encryption.AesEmbeddedEncoder
import com.github.shahrivari.redipper.base.encoding.encryption.AesEncoder
import com.github.shahrivari.redipper.util.AppTestUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CombinedEncoderTest : AppTestUtils {
    @Test
    internal fun `test encode and decode`() {
        val plainString = randomString(17)
        val aesEncoder = AesEncoder(plainString)
        val embeddedEncoder = AesEmbeddedEncoder()
        val gzipEncoder = GzipEncoder()
        val lz4Encoder = Lz4Encoder()

        val of = CombinedEncoder.of(gzipEncoder, lz4Encoder, aesEncoder, embeddedEncoder)
        val bytes = of.encode(plainString.toByteArray())
        val decode = String(of.decode(bytes))

        Assertions.assertThat(plainString).isEqualTo(decode)
    }
}