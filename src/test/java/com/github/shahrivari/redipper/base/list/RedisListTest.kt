package com.github.shahrivari.redipper.base.list

import com.github.shahrivari.redipper.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(RedisTest::class, RedisCacheTest::class)
internal class RedisListTest : RedisListUtils {

    @Test
    internal fun `set and get correctly`() {
        val redisList = buildRedisListTest<RedisCacheUtils.Person>()
        val person = createPerson()

        redisList.lpush(person.id.toString(), person)
        val getPerson = redisList.lpop(person.id.toString())

        assertThat(getPerson).isNotNull
        assertThat(person.name).isEqualTo(getPerson!!.name)
        assertThat(person.id).isEqualTo(getPerson.id)
        assertThat(person.phone).isEqualTo(getPerson.phone)
    }

    @Test
    internal fun `set and get all elements of list`() {
        val redisList = buildRedisListTest<RedisCacheUtils.Person>()

        val key = "testList"
        repeat((1..5).count()) {
            val person = createPerson()
            redisList.lpush(key, person)
        }

        val getPersons = redisList.getAll(key)

        assertThat(getPersons).isNotNull
        assertThat(getPersons.size).isEqualTo(5)
        assertThat(redisList.llen(key)).isEqualTo(5)
    }

    @Test
    internal fun `set and get last element of list`() {
        val redisList = buildRedisListTest<RedisCacheUtils.Person>()

        val key = "testList"
        repeat((1..5).count()) {
            val person = createPerson()
            redisList.lpush(key, person)
        }

        val getPersons = redisList.getAll(key)

        val len = redisList.llen(key)
        assertThat(getPersons).isNotNull
        assertThat(getPersons.size).isEqualTo(5)
        assertThat(len).isEqualTo(5)

        redisList.rpop(key)
        assertThat(redisList.llen(key)).isEqualTo(4)
    }

    @Test
    internal fun `duplicate space should not be correct`() {
        val space = "testName"
        buildRedisListTest<String>(space)

        assertThrows<IllegalArgumentException> {
            buildRedisListTest<String>(space)
        }
    }

    @Test
    internal fun `duplicate space with force parameter should be correct`() {
        val space = "testName"
        buildRedisListTest<String>(space)
        buildRedisListTest<String>(space, true)
    }

    @Test
    internal fun `set value with ttl twice`() {
        val mapTest = buildRedisListTest<String>(duration = 40, unit = TimeUnit.SECONDS)
        val key = "key"

        mapTest.lpush(key, "value1")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)

        Thread.sleep(2000)
        assertThat(mapTest.getTtl(key)).isEqualTo(38)

        mapTest.lpush(key, "value2")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)
    }
}