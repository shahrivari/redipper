package com.github.shahrivari.redipper.base.sortedSet

import com.github.shahrivari.redipper.util.RedisCacheTest
import com.github.shahrivari.redipper.util.RedisSortedSetUtils
import com.github.shahrivari.redipper.util.RedisTest
import com.github.shahrivari.redipper.util.buildRedisSortedSetTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(RedisTest::class, RedisCacheTest::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RedisSortedSetTest : RedisSortedSetUtils {

    @Test
    internal fun `set and get correctly`() {
        val set = buildRedisSortedSetTest<Int>()

        set.apply {
            zadd(1.toString(), Pair(1.toDouble(), 1111))
            zadd(1.toString(), Pair(4.toDouble(), 2222))
            zadd(1.toString(), Pair(3.toDouble(), 3333))
            zadd(1.toString(), Pair(2.toDouble(), 4444))
        }
        repeat(5) {
            set.zadd(1.toString(), Pair(5.toDouble(), 5555))
        }

        val list = set.zrange(1.toString(), 0, -1)
        assertThat(list.size).isEqualTo(5)
    }
}
