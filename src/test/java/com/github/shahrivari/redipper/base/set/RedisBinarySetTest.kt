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
internal class RedisBinarySetTest : RedisBinarySetUtils {

    @Test
    internal fun `should be able to sadd and smembers kv (value is not tl)`() {
        val userCache = buildRedisBinarySetTest<RedisCacheUtils.Person>()
        val person = createPerson()

        saddTest(userCache, person.id.toString(), person)
        val user = smembersTest(userCache, person.id.toString()).first()

        assertNotNull(user)
        assertThat(person.id).isEqualTo(user.id)
        assertThat(person.phone).isEqualTo(user.phone)
        assertThat(person.name).isEqualTo(user.name)
    }

    @Test
    internal fun `should be able to srem kv (value as tl)`() {
        val userCache = buildRedisBinarySetTest<RedisCacheUtils.Person>()
        val person = createPerson()

        saddTest(userCache, person.id.toString(), person)
        val p1 = smembersTest(userCache, person.id.toString())

        assertThat(p1.size).isEqualTo(1)
        assertThat(p1[0].name).isEqualTo(person.name)
        assertThat(p1[0].phone).isEqualTo(person.phone)

        sremTest(userCache, person.id.toString(), person)
        val p2 = smembersTest(userCache, person.id.toString())

        assertThat(p2.size).isEqualTo(0)
    }

    @Test
    internal fun `should be able to srem kv (value is not tl)`() {
        val userCache = buildRedisBinarySetTest<RedisCacheUtils.Person>()
        val userA = createPerson()
        val userB = createPerson()

        saddTest(userCache, userA.id.toString(), userA)
        saddTest(userCache, userA.id.toString(), userB)

        val user = smembersTest(userCache, userA.id.toString())

        assertNotNull(user)
        assertThat(user.size).isEqualTo(2)

        sremTest(userCache, userA.id.toString(), userB)

        val user1 = smembersTest(userCache, userA.id.toString())

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
        buildRedisBinarySetTest<String>(space)

        assertThrows<IllegalArgumentException> {
            buildRedisBinarySetTest<String>(space)
        }
    }

    @Test
    internal fun `duplicate space with force parameter should be correct`() {
        val space = "testName"
        buildRedisBinarySetTest<String>(space)
        buildRedisBinarySetTest<String>(space, true)
    }

    @Test
    internal fun `set value with ttl twice`() {
        val mapTest = buildRedisBinarySetTest<String>(duration = 40, unit = TimeUnit.SECONDS)
        val key = "key"

        saddTest(mapTest, key, "value1")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)

        Thread.sleep(5000)
        assertThat(mapTest.getTtl(key)).isEqualTo(35)

        saddTest(mapTest, key, "value2")
        assertThat(mapTest.getTtl(key)).isEqualTo(40)
    }
}