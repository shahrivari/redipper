package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.sortedSet.RedisSortedSet
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisSortedSetUtils : AppTestUtils, RedisCacheUtils {
    fun <V : Serializable> zaddTest(redisCache: RedisSortedSet<V>, key: String, value: Pair<Int, V>) =
            redisCache.zadd(key, value.first.toDouble() to value.second)

    fun <V : Serializable> zremTest(redisCache: RedisSortedSet<V>, key: String, value: V): Long? =
            redisCache.zrem(key, value)

    fun <V : Serializable> zrangeTest(redisCache: RedisSortedSet<V>,
                                      key: String,
                                      start: Int,
                                      stop: Int): List<V?> =
            redisCache.zrange(key, start, stop)
}

inline fun <reified V : Serializable> RedisSortedSetUtils.buildRedisBinarySetTest(space: String = randomName,
                                                                                  forceSpace: Boolean = false,
                                                                                  duration: Long = 1,
                                                                                  unit: TimeUnit = TimeUnit.MINUTES,
                                                                                  vararg encoder: Encoder): RedisSortedSet<V> {
    return RedisSortedSet.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
            .withTtl(duration, unit)
            .withEncoder(*encoder)
            .build()
}