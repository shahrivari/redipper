package com.github.shahrivari.redipper.base.list

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.github.shahrivari.redipper.util.RedisCacheUtils
import com.github.shahrivari.redipper.util.RedisListUtils
import com.github.shahrivari.redipper.util.RedisTest

@ExtendWith(RedisTest::class)
internal class RedisListTest : RedisListUtils {

    @Test
    internal fun `set and get correctly`() {
        val redisList = buildRedisListTest("person", RedisCacheUtils.Person::class.java)
        val person = createPerson()

        lpushTest(redisList, person.id.toString(), person)
        val getPerson = lpopTest(redisList, person.id.toString())

        assertThat(getPerson).isNotNull
        assertThat(person.name).isEqualTo(getPerson!!.name)
        assertThat(person.id).isEqualTo(getPerson.id)
        assertThat(person.phone).isEqualTo(getPerson.phone)
    }

    @Test
    internal fun `set and get all elements of list`() {
        val redisList = buildRedisListTest("person", RedisCacheUtils.Person::class.java)

        val key = "testList"
        repeat((1..5).count()) {
            val person = createPerson()
            lpushTest(redisList, key, person)
        }

        val getPersons = getAllTest(redisList, key)

        assertThat(getPersons).isNotNull
        assertThat(getPersons.size).isEqualTo(5)
        assertThat(llenTest(redisList, key)).isEqualTo(5)
    }

    @Test
    internal fun `set and get last element of list`() {
        val redisList = buildRedisListTest("person", RedisCacheUtils.Person::class.java)

        val key = "testList"
        repeat((1..5).count()) {
            val person = createPerson()
            lpushTest(redisList, key, person)
        }

        val getPersons = getAllTest(redisList, key)

        val len = llenTest(redisList, key)
        assertThat(getPersons).isNotNull
        assertThat(getPersons.size).isEqualTo(5)
        assertThat(len).isEqualTo(5)

        rpopTest(redisList, key)
        assertThat(llenTest(redisList, key)).isEqualTo(4)
    }
}