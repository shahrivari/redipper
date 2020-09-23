package com.github.shahrivari.redipper.base.set

import com.github.shahrivari.redipper.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

@ExtendWith(RedisTest::class, RedisCacheTest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RedisSetTest : RedisSetUtils {

    @Test
    internal fun `should be able to sadd and smembers kv (value is not tl)`() {
        val userCache = buildRedisSetTest<RedisCacheUtils.Person>()
        val person = createPerson()

        userCache.sadd(person.id.toString(), person)
        val user = userCache.smembers(person.id.toString()).first()

        assertNotNull(user)
        assertThat(person.id).isEqualTo(user.id)
        assertThat(person.phone).isEqualTo(user.phone)
        assertThat(person.name).isEqualTo(user.name)
    }

    @Test
    internal fun `should be able to srem kv (value as tl)`() {
        val userCache = buildRedisSetTest<RedisCacheUtils.Person>()
        val person = createPerson()

        userCache.sadd(person.id.toString(), person)
        val p1 = userCache.smembers(person.id.toString()).toList()

        assertThat(p1.size).isEqualTo(1)
        assertThat(p1[0].name).isEqualTo(person.name)
        assertThat(p1[0].phone).isEqualTo(person.phone)

        userCache.srem(person.id.toString(), person)
        val p2 = userCache.smembers(person.id.toString())

        assertThat(p2.size).isEqualTo(0)
    }

    @Test
    internal fun `should be able to srem kv (value is not tl)`() {
        val userCache = buildRedisSetTest<RedisCacheUtils.Person>()
        val userA = createPerson()
        val userB = createPerson()

        userCache.sadd(userA.id.toString(), userA)
        userCache.sadd(userA.id.toString(), userB)

        val user = userCache.smembers(userA.id.toString())

        assertNotNull(user)
        assertThat(user.size).isEqualTo(2)

        userCache.srem(userA.id.toString(), userB)

        val user1 = userCache.smembers(userA.id.toString())

        assertNotNull(user1)
        assertThat(user1.size).isEqualTo(1)

        val first = user1.first()
        assertThat(first.id).isEqualTo(userA.id)
        assertThat(first.phone).isEqualTo(userA.phone)
        assertThat(first.name).isEqualTo(userA.name)
    }

    @Test
    internal fun `duplicate space should not be correct`() {
        val space = "testName"
        buildRedisSetTest<String>(space)

        assertThrows<IllegalArgumentException> {
            buildRedisSetTest<String>(space)
        }
    }

    @Test
    internal fun `duplicate space with force parameter should be correct`() {
        val space = "testName"
        buildRedisSetTest<String>(space)
        buildRedisSetTest<String>(space, true)
    }

    @Test
    internal fun `set value with ttl twice`() {
        val mapTest = buildRedisSetTest<String>(duration = 40, unit = TimeUnit.SECONDS)
        val key = "key"

        mapTest.sadd(key, "value1")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)

        Thread.sleep(2000)
        assertThat(mapTest.getTtl(key)).isEqualTo(38)

        mapTest.sadd(key, "value2")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)
    }

    @Test
    internal fun `put and get with loader`() {
        val persons = listOf(createPerson(), createPerson())
        val userCache = buildRedisSetTest("testName") { persons }
        val users = userCache.smembers(randomName).toList()
        assertThat(users.size).isEqualTo(2)
        assertThat(users[0].id).isEqualTo(persons[0].id)
        assertThat(users[1].id).isEqualTo(persons[1].id)
    }

    @Test
    internal fun `should be able to delete kv from cache (value is not tl)`() {
        val userCache = buildRedisSetTest<RedisCacheUtils.Person>()
        val person = createPerson()

        userCache.sadd(person.id.toString(), person)
        val members = userCache.smembers(person.id.toString())
        assertThat(members.size).isEqualTo(1)

        userCache.del(person.id.toString())
        val user = userCache.smembers(person.id.toString())
        assertThat(user.size).isEqualTo(0)
    }
}