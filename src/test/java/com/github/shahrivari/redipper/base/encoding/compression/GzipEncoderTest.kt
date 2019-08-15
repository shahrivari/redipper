package com.github.shahrivari.redipper.base.encoding.compression

import com.github.shahrivari.redipper.util.AppTestUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class GzipEncoderTest : AppTestUtils {

    @Test
    internal fun `test encode and decode`() {
        val plainString = randomString(Random.nextInt(20))
        val gzipEncoder = GzipEncoder()

        val bytes = gzipEncoder.encode(plainString.toByteArray())
        val decode = String(gzipEncoder.decode(bytes))

        Assertions.assertThat(plainString).isEqualTo(decode)
    }
}