package com.github.shahrivari.redipper.base.sortedSet

import com.github.shahrivari.redipper.util.RedisCacheTest
import com.github.shahrivari.redipper.util.RedisSortedSetUtils
import com.github.shahrivari.redipper.util.RedisTest
import com.github.shahrivari.redipper.util.buildRedisBinarySetTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(RedisTest::class, RedisCacheTest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RedisSortedSetTest : RedisSortedSetUtils {

    @Test
    internal fun `set and get correctly`() {
        val set = buildRedisBinarySetTest<Int>()

        zaddTest(set, 1.toString(), Pair(1, 1111))
        zaddTest(set, 1.toString(), Pair(4, 2222))
        zaddTest(set, 1.toString(), Pair(3, 3333))
        zaddTest(set, 1.toString(), Pair(2, 4444))
        repeat(5) {
            zaddTest(set, 1.toString(), Pair(5, 5555))
        }

        val list = zrangeTest(set, 1.toString(), 0, -1)
        assertThat(list.size).isEqualTo(5)
    }
}
