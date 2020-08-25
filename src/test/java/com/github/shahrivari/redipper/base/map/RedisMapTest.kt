package com.github.shahrivari.redipper.base.map

import com.github.shahrivari.redipper.base.encoding.encryption.AesEncoder
import com.github.shahrivari.redipper.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


@ExtendWith(RedisTest::class, RedisCacheTest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RedisMapTest : RedisMapUtils {

    @Test
    internal fun `semicolon in key is correctly handled`() {
        val userCache = buildRedisMapTest<String>()
        val key = "kashk:askk"
        val value = "salam"
        userCache.set(key, value)
        assertThat(userCache.mget(listOf(key)).map { it.key to it.value })
                .containsExactly(Pair(key, value))
    }

    @Test
    internal fun `semicolon is not permitted in space`() {
        assertThrows<IllegalArgumentException> { buildRedisMapTest<String>("user:alaki") }
    }

    @Test
    internal fun `should be able to set and get kv (value is not tl)`() {
        val userCache = buildRedisMapTest<RedisCacheUtils.Person>()
        val person = createPerson()

        setTest(userCache, person.id.toString(), person)
        val test = getTest(userCache, person.id.toString())

        assertNotNull(test)
        assertThat(test.id).isEqualTo(person.id)
        assertThat(test.name).isEqualTo(person.name)
        assertThat(test.phone).isEqualTo(person.phone)
    }

    @Test
    internal fun `null in mget`() {
        val userCache = buildRedisMapTest<RedisCacheUtils.Person>()
        val original =
                (1..10).map { if (it % 2 == 0) createPerson() else null }
                        .associateBy { it?.id?.toString() ?: Random.nextLong().toString() }

        msetTest(userCache, original)
        val map = mgetTest(userCache, original.keys)

        map.forEach { (key, value) ->
            assertThat(value).isEqualTo(original[key])
        }

        val nonExisting = (1..5).map { Random.nextLong().toString() }
        assertTrue(mgetTest(userCache, nonExisting).isEmpty())
    }

    @Test
    internal fun `should be able to set and get multiple kvs (value is not tl)`() {
        val userCache = buildRedisMapTest<RedisCacheUtils.Person>()

        val userList = mutableMapOf<String, RedisCacheUtils.Person>()
        repeat(10) {
            val user = createPerson()
            userList[user.id.toString()] = user
        }

        msetTest(userCache, userList)
        val map = mgetTest(userCache, userList.keys)

        map.forEach { (key, value) ->
            assertThat(value?.name).isEqualTo(userList[key]!!.name)
            assertThat(value?.id).isEqualTo(userList[key]!!.id)
            assertThat(value?.phone).isEqualTo(userList[key]!!.phone)
        }
    }

    @Test
    internal fun `should be able to delete kv from cache (value is not tl)`() {
        val userCache = buildRedisMapTest<RedisCacheUtils.Person>()
        val person = createPerson()

        setTest(userCache, person.id.toString(), person)
        delTest(userCache, person.id.toString())

        val test = getTest(userCache, person.id.toString())
        assertThat(test).isEqualTo(null)
    }

    @Test
    internal fun `should get null if key not exist in cache (value si not tl)`() {
        val userCache = buildRedisMapTest<RedisCacheUtils.Person>()
        val user = createPerson()

        val test = getTest(userCache, user.id.toString())
        assertNull(test)
    }

    @Test
    internal fun `test loader for person`() {
        val result = createPerson()
        val userCache = buildRedisMapTest(loader = { result })

        val user = createPerson()
        val test = getTest(userCache, user.id.toString())

        assertNotNull(test)
        assertThat(test.id).isEqualTo(result.id)
        assertThat(test.name).isEqualTo(result.name)
        assertThat(test.phone).isEqualTo(result.phone)
    }

    @Test
    internal fun `should be able set and get with encryption`() {
        val result = createPerson()
        val plainString = randomString(17)
        val aes128 = AesEncoder(plainString)
        val userCache =
                buildRedisMapTest(loader = { result }, encoder = *arrayOf(aes128))

        val user = createPerson()
        val test = getTest(userCache, user.id.toString())

        assertNotNull(test)
        assertThat(test.id).isEqualTo(result.id)
        assertThat(test.name).isEqualTo(result.name)
        assertThat(test.phone).isEqualTo(result.phone)
    }

    @Test
    internal fun `shoud be able to inc and dec int cache`() {
        val redisMap = buildRedisMapTest<Int>()
        val key = "ali"
        redisMap.set(key, 5)

        redisMap.incr(key)
        assertThat(redisMap.get(key)).isEqualTo(6)

        redisMap.decr(key)
        assertThat(redisMap.get(key)).isEqualTo(5)
    }

    @Test
    internal fun `shoud be able to inc and dec long cache`() {
        val redisMap = buildRedisMapTest<Long>()
        val key = "ali"
        redisMap.set(key, 5)

        redisMap.incr(key)
        assertThat(redisMap.get(key)).isEqualTo(6)

        redisMap.decr(key)
        assertThat(redisMap.get(key)).isEqualTo(5)
    }

    @Test
    internal fun `duplicate space should not be correct`() {
        val space = "testName"
        buildRedisMapTest<String>(space)

        assertThrows<IllegalArgumentException> {
            buildRedisMapTest<String>(space)
        }
    }

    @Test
    internal fun `duplicate space with force parameter should be correct`() {
        val space = "testName"
        buildRedisMapTest<String>(space)
        buildRedisMapTest<String>(space, true)
    }

    @Test
    internal fun `set value with ttl twice`() {
        val mapTest = buildRedisMapTest<String>(duration = 40, unit = TimeUnit.SECONDS)
        val key = "key"

        setTest(mapTest, key, "value1")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)

        Thread.sleep(2000)
        assertThat(mapTest.getTtl(key)).isEqualTo(38)

        setTest(mapTest, key, "value2")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)
    }

    @Test
    internal fun `clear all map`() {
        val mapTest = buildRedisMapTest<String>(duration = 40, unit = TimeUnit.SECONDS)
        val key = "key"
        mapTest.set(key, key)
        assertThat(mapTest.keys()).containsExactly(key)
        mapTest.clear()
        assertThat(mapTest.keys()).isEmpty()
    }

    @Test
    internal fun `enumerate keys`() {
        val mapTest = buildRedisMapTest<String>(duration = 40, unit = TimeUnit.SECONDS)
        assertThat(mapTest.keys()).isEmpty()
        val key = "key"
        mapTest.set(key, key)
        assertThat(mapTest.keys()).containsExactly(key)

        mapTest.set(key, key)
        assertThat(mapTest.keys()).containsExactly(key)

        mapTest.set(key + key, key)
        assertThat(mapTest.keys()).containsExactly(key, key + key)
    }

    @Test
    internal fun `invalidate whole cache blocking server`() {
        val mapTest =
                buildRedisMapTest<String>(duration = 1, unit = TimeUnit.MINUTES, space = "abri")
        assertThat(mapTest.keys()).isEmpty()

        val key = "key"

        repeat(5) { mapTest.set(key + it, key + it) }

        repeat(5) {
            val get = mapTest.get(key + it)
            assertThat(get).isEqualTo(key + it)
        }

        assertThrows<IllegalArgumentException> {
            buildRedisMapTest<String>(duration = 1, unit = TimeUnit.MINUTES, space = "abri")
        }

        mapTest.invalidateWholeCache()

        assertThat(mapTest.keys()).isEmpty()

        val redis = buildRedisMapTest<String>(duration = 1, unit = TimeUnit.MINUTES, space = "abri")

        repeat(5) { redis.set(key + it, key + it) }

        repeat(5) {
            val get = redis.get(key + it)
            assertThat(get).isEqualTo(key + it)
        }
    }
}