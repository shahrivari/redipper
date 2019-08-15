package com.github.shahrivari.redipper.base.map

import com.github.shahrivari.redipper.base.encoding.encryption.AesEncoder
import com.github.shahrivari.redipper.util.RedisCacheUtils
import com.github.shahrivari.redipper.util.RedisMapUtils
import com.github.shahrivari.redipper.util.RedisTest
import io.objects.tl.api.TLUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.random.Random
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@ExtendWith(RedisTest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RedisMapTest : RedisMapUtils {

    @Test
    internal fun `semicolon in key is correctly handled`() {
        val userCache = buildRedisMapTest("user", String::class.java)
        val key = "kashk:askk"
        val value = "salam"
        userCache.set(key, value)
        assertThat(userCache.mget(listOf(key)).map { it.key to it.value })
                .containsExactly(Pair(key, value))
    }

    @Test
    internal fun `semicolon is not permitted in space`() {
        assertThrows<IllegalArgumentException> { buildRedisMapTest("user:alaki", String::class.java) }
    }

    @Test
    internal fun `should be able to set and get kv (value is not tl)`() {
        val userCache = buildRedisMapTest("person", RedisCacheUtils.Person::class.java)
        val person = createPerson()

        setTest(userCache, person.id.toString(), person)
        val test = getTest(userCache, person.id.toString())

        kotlin.test.assertNotNull(test)
        assertThat(test.id).isEqualTo(person.id)
        assertThat(test.name).isEqualTo(person.name)
        assertThat(test.phone).isEqualTo(person.phone)
    }

    @Test
    internal fun `should be able to set and get multiple kvs (value is not tl)`() {
        val userCache = buildRedisMapTest("person", RedisCacheUtils.Person::class.java)

        val userList = mutableMapOf<String, RedisCacheUtils.Person>()
        repeat(10) {
            val user = createPerson()
            userList[user.id.toString()] = user
        }

        msetTest(userCache, userList)
        val map = mgetTest(userCache, userList.keys)

        map.forEach { key, value ->
            assertThat(value.name).isEqualTo(userList[key]!!.name)
            assertThat(value.id).isEqualTo(userList[key]!!.id)
            assertThat(value.phone).isEqualTo(userList[key]!!.phone)
        }
    }

    @Test
    internal fun `should be able to delete kv from cache (value is not tl)`() {
        val userCache = buildRedisMapTest("person", RedisCacheUtils.Person::class.java)
        val person = createPerson()

        setTest(userCache, person.id.toString(), person)
        delTest(userCache, person.id.toString())

        val test = getTest(userCache, person.id.toString())
        assertThat(test).isEqualTo(null)
    }

    @Test
    internal fun `should get null if key not exist in cache (value si not tl)`() {
        val userCache = buildRedisMapTest("person", RedisCacheUtils.Person::class.java)
        val user = createPerson()

        val test = getTest(userCache, user.id.toString())
        assertNull(test)
    }

    @Test
    internal fun `test loader for person`() {
        val result = createPerson()
        val userCache =
                buildRedisMapTest("person", RedisCacheUtils.Person::class.java, l = { result })

        val user = createPerson()
        val test = getTest(userCache, user.id.toString())

        assertNotNull(test)
        assertThat(test.id).isEqualTo(result.id)
        assertThat(test.name).isEqualTo(result.name)
        assertThat(test.phone).isEqualTo(result.phone)
    }

    @Test
    internal fun `test loader for tl`() {
        val userCache = buildRedisMapTest("user", TLUser::class.java)
        val test = getTest(userCache, randomPhone)
        assertNull(test)
    }

    @Test
    internal fun `should be able set and get with encryption`() {
        val result = createPerson()
        val plainString = randomString(17)
        val aes128 = AesEncoder(plainString)
        val userCache =
                buildRedisMapTest("person", RedisCacheUtils.Person::class.java, l = { result }, encoder = aes128)

        val user = createPerson()
        val test = getTest(userCache, user.id.toString())

        assertNotNull(test)
        assertThat(test.id).isEqualTo(result.id)
        assertThat(test.name).isEqualTo(result.name)
        assertThat(test.phone).isEqualTo(result.phone)
    }
}