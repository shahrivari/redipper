package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.sortedSet.RedisSortedSet
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisSortedSetUtils : AppTestUtils, RedisCacheUtils

inline fun <reified V : Serializable> RedisSortedSetUtils.buildRedisSortedSetTest(space: String = randomName,
                                                                                  forceSpace: Boolean = false,
                                                                                  duration: Long = 1,
                                                                                  unit: TimeUnit = TimeUnit.MINUTES,
                                                                                  vararg encoder: Encoder)
        : RedisSortedSet<V> {
    return RedisSortedSet.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
            .withTtl(duration, unit)
            .withEncoder(*encoder)
            .build()
}