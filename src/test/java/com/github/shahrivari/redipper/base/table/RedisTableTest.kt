package com.github.shahrivari.redipper.base.table

import com.github.shahrivari.redipper.base.encoding.compression.GzipEncoder
import com.github.shahrivari.redipper.base.encoding.compression.Lz4Encoder
import com.github.shahrivari.redipper.base.encoding.encryption.AesEmbeddedEncoder
import com.github.shahrivari.redipper.base.encoding.encryption.AesEncoder
import com.github.shahrivari.redipper.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(RedisTest::class, RedisCacheTest::class)
internal class RedisTableTest : RedisTableUtils {

    @Test
    internal fun `set and get correctly`() {
        val redisTable = buildRedisTableTest<RedisCacheUtils.Person>()
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

        val redisTable = buildRedisTableTest<RedisCacheUtils.Person>(encoder = *arrayOf(aes128,
                                                                                        embeddedEncoder,
                                                                                        gzipEncoder,
                                                                                        lz4Encoder))

        val person = createPerson()

        val field = "test"
        hsetTest(redisTable, person.id.toString(), field, person)

        val getPerson = hgetAllTest(redisTable, person.id.toString(), field)

        assertThat(getPerson.values.toList().first()!!.phone).isEqualTo(person.phone)
        assertThat(getPerson.values.toList().first()!!.id).isEqualTo(person.id)
        assertThat(getPerson.values.toList().first()!!.name).isEqualTo(person.name)
    }

    @Test
    internal fun `duplicate space should not be correct`() {
        val space = "testName"
        buildRedisTableTest<String>(space)

        assertThrows<IllegalArgumentException> {
            buildRedisTableTest<String>(space)
        }
    }

    @Test
    internal fun `duplicate space with force parameter should be correct`() {
        val space = "testName"
        buildRedisTableTest<String>(space)
        buildRedisTableTest<String>(space, true)
    }

    @Test
    @Disabled("Expire of hash not supported in jedis mock.")
    internal fun `set value with ttl twice`() {
        val redisTable =
                buildRedisTableTest<RedisCacheUtils.Person>(duration = 40, unit = TimeUnit.SECONDS)

        val person = createPerson()
        val field = "test"

        hsetTest(redisTable, person.id.toString(), field, person)
        assertThat(redisTable.getTtl(person.id.toString())).isEqualTo(40)

        Thread.sleep(5000)
        assertThat(redisTable.getTtl(person.id.toString())).isEqualTo(35)

        hsetTest(redisTable, person.id.toString(), field, person)
        assertThat(redisTable.getTtl(person.id.toString())).isEqualTo(40)
    }
}