package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.set.RedisBinarySet
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisBinarySetUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> saddTest(redisCache: RedisBinarySet<V>, key: String, value: V) =
            redisCache.sadd(key, value)

    fun <V : Serializable> sremTest(redisCache: RedisBinarySet<V>, key: String, value: V): Long? =
            redisCache.srem(key, value)

    fun <V : Serializable> smembersTest(redisCache: RedisBinarySet<V>, key: String): List<V> =
            redisCache.smembers(key)
}

inline fun <reified V : Serializable> RedisBinarySetUtils.buildRedisBinarySetTest(space: String = randomName,
                                                                                  forceSpace: Boolean = false,
                                                                                  duration: Long = 1,
                                                                                  unit: TimeUnit = TimeUnit.MINUTES,
                                                                                  vararg encoder: Encoder): RedisBinarySet<V> {
    return RedisBinarySet.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
            .withTtl(duration, unit)
            .withEncoder(*encoder)
            .build()
}