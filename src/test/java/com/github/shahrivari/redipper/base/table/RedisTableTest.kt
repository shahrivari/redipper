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
import kotlin.test.assertNotNull

@ExtendWith(RedisTest::class, RedisCacheTest::class)
internal class RedisTableTest : RedisTableUtils {

    @Test
    @Disabled("hlen not support")
    internal fun `set and get correctly`() {
        val redisTable = buildRedisTableTest<RedisCacheUtils.Person>()
        val person = createPerson()

        val field = "test"
        hsetTest(redisTable, person.id.toString(), field, person)
        val getPerson = hmgetTest(redisTable, person.id.toString(), field)

        assertThat(getPerson.values.toList().first()!!.phone).isEqualTo(person.phone)
        assertThat(getPerson.values.toList().first()!!.id).isEqualTo(person.id)
        assertThat(getPerson.values.toList().first()!!.name).isEqualTo(person.name)
    }

    @Test
    @Disabled("hlen not support")
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

        val getPerson = hmgetTest(redisTable, person.id.toString(), field)

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
        val redisTable = buildRedisTableTest<RedisCacheUtils.Person>(duration = 40, timeUnit = TimeUnit.SECONDS)

        val person = createPerson()
        val field = "test"

        hsetTest(redisTable, person.id.toString(), field, person)
        assertThat(redisTable.getTtl(person.id.toString())).isEqualTo(40)

        Thread.sleep(5000)
        assertThat(redisTable.getTtl(person.id.toString())).isEqualTo(35)

        hsetTest(redisTable, person.id.toString(), field, person)
        assertThat(redisTable.getTtl(person.id.toString())).isEqualTo(40)
    }

    @Test
    @Disabled("hlen not support")
    internal fun `should eliminate null values`() {
        val redisTable = buildRedisTableTest<RedisCacheUtils.Person>(duration = 40, timeUnit = TimeUnit.SECONDS)
        val person = createPerson()
        val field = "test"

        hsetTest(redisTable, person.id.toString(), field, person)
        val map = hmgetTest(redisTable, person.id.toString(), field, "alaki")
        assertThat(map.size).isEqualTo(1)
    }

    @Test
    @Disabled("hlen not support")
    internal fun `test loader for person`() {
        val result = mapOf("test" to createPerson())
        val userCache = buildRedisTableTest(loader = { result })

        val user = createPerson()
        val test = hgetTest(userCache, user.id.toString(), "test")

        assertNotNull(test)
        assertThat(test.id).isEqualTo(result.values.first().id)
        assertThat(test.name).isEqualTo(result.values.first().name)
        assertThat(test.phone).isEqualTo(result.values.first().phone)
    }

    @Test
    @Disabled("hlen not support")
    internal fun `should eliminate null values when get all fields`() {
        val redisTable = buildRedisTableTest<RedisCacheUtils.Person>(duration = 40, timeUnit = TimeUnit.SECONDS)
        val key = "alaki"

        val p1 = createPerson()
        val p2 = createPerson()
        val p3 = createPerson()
        val p4 = createPerson()

        val f1 = "test1"
        val f2 = "test2"
        val f3 = "test3"
        val f4 = "test4"
        val f5 = "test5"

        hsetTest(redisTable, key, f1, p1)
        hsetTest(redisTable, key, f2, p2)
        hsetTest(redisTable, key, f3, p3)

        val map = hgetAllTest(redisTable, key)
        assertThat(map.size).isEqualTo(3)

        hsetTest(redisTable, key, f4, p4)
        hsetTest(redisTable, key, f5, null)

        val map2 = hgetAllTest(redisTable, key)
        assertThat(map2.size).isEqualTo(5)
    }

    @Test
    @Disabled("hlen not support")
    internal fun `hset should work correctly`() {
        val redisTable = buildRedisTableTest<Int>()

        val fieldValue = mapOf("f1" to 1, "f2" to 2, "f3" to 3, "f5" to null)
        hmsetTest(redisTable, "key", fieldValue)

        val map = hgetAllTest(redisTable, "key")
        assertThat(map.size).isEqualTo(4)
    }

    @Disabled("hlen and hdel(multiple delete) not support")
    @Test
    internal fun `all fields should be delete from cache`() {
        val redisTable = buildRedisTableTest<Int>()
        val fieldValue = mapOf("f1" to 1,
                               "f2" to 2,
                               "f3" to 3,
                               "f5" to null,
                               "f6" to 6,
                               "f7" to 7,
                               "f8" to null)

        hmsetTest(redisTable, "key", fieldValue)

        hsetTest(redisTable, "key2", "f1", 1)

        val key = hgetAllTest(redisTable, "key")
        val key2 = hgetAllTest(redisTable, "key2")

        assertThat(key.keys.size).isEqualTo(7)
        assertThat(key2.keys.size).isEqualTo(1)

        val hkeysAllTest = hkeysAllTest(redisTable, "key")
        hdelTest(redisTable, "key", *hkeysAllTest.toTypedArray())

        val keyAfterDel = hgetAllTest(redisTable, "key")
        val key2AfterDel = hgetAllTest(redisTable, "key2")

        assertThat(keyAfterDel.keys.size).isEqualTo(0)
        assertThat(key2AfterDel.keys.size).isEqualTo(1)
    }
}