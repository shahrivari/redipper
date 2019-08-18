package com.github.shahrivari.redipper.base.table

import com.github.shahrivari.redipper.base.encoding.compression.GzipEncoder
import com.github.shahrivari.redipper.base.encoding.compression.Lz4Encoder
import com.github.shahrivari.redipper.base.encoding.encryption.AesEmbeddedEncoder
import com.github.shahrivari.redipper.base.encoding.encryption.AesEncoder
import com.github.shahrivari.redipper.util.RedisCacheUtils
import com.github.shahrivari.redipper.util.RedisTableUtils
import com.github.shahrivari.redipper.util.RedisTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RedisTest::class)
internal class RedisTableTest : RedisTableUtils {

    @Test
    internal fun `set and get correctly`() {
        val redisTable = buildRedisTableTest("person", RedisCacheUtils.Person::class.java)
        val person = createPerson()

        val field = "test"
        hsetTest(redisTable, person.id.toString(), field, person)
        val getPerson = hgetAllTest(redisTable, person.id.toString(), field)

        assertThat(getPerson.values.toList().first()!!.phone).isEqualTo(person.phone)
        assertThat(getPerson.values.toList().first()!!.id).isEqualTo(person.id)
        assertThat(getPerson.values.toList().first()!!.name).isEqualTo(person.name)
    }

    @Test
    internal fun `set and get with encryption`() {
        val plainString = randomString(20)
        val aes128 = AesEncoder(plainString)
        val embeddedEncoder = AesEmbeddedEncoder()
        val gzipEncoder = GzipEncoder()
        val lz4Encoder = Lz4Encoder()

        val redisTable =
                buildRedisTableTest("person", RedisCacheUtils.Person::class.java,
                        encoder = *arrayOf(aes128, embeddedEncoder, gzipEncoder, lz4Encoder))

        val person = createPerson()

        val field = "test"
        hsetTest(redisTable, person.id.toString(), field, person)

        val getPerson = hgetAllTest(redisTable, person.id.toString(), field)

        assertThat(getPerson.values.toList().first()!!.phone).isEqualTo(person.phone)
        assertThat(getPerson.values.toList().first()!!.id).isEqualTo(person.id)
        assertThat(getPerson.values.toList().first()!!.name).isEqualTo(person.name)
    }
}