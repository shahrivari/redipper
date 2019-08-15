package com.github.shahrivari.redipper.base.encoding.compression

import com.github.shahrivari.redipper.util.AppTestUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class Lz4EncoderTest : AppTestUtils {
    @Test
    internal fun `test encode and decode`() {
        val plainString = randomString(Random.nextInt(20))
        val lz4Encoder = Lz4Encoder()

        val bytes = lz4Encoder.encode(plainString.toByteArray())
        val decode = String(lz4Encoder.decode(bytes))

        Assertions.assertThat(plainString).isEqualTo(decode)
    }
}