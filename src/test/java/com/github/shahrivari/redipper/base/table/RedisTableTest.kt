package com.github.shahrivari.redipper.base.table

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.github.shahrivari.redipper.util.RedisCacheUtils
import com.github.shahrivari.redipper.util.RedisTableUtils
import com.github.shahrivari.redipper.util.RedisTest

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
}