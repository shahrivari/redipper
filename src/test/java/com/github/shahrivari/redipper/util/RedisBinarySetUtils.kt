package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.set.RedisBinarySet
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisBinarySetUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> buildRedisBinarySetTest(space: String,
                                                   clazz: Class<V>,
                                                   duration: Long = 1,
                                                   unit: TimeUnit = TimeUnit.MINUTES): RedisBinarySet<V> {
        return RedisBinarySet.Builder(RedisTest.redisConfig, space, clazz)
                .withTtl(duration, unit)
                .build()
    }

    fun <V : Serializable> saddTest(redisCache: RedisBinarySet<V>, key: String, value: V) {
        return redisCache.sadd(key, value)
    }

    fun <V : Serializable> sremTest(redisCache: RedisBinarySet<V>, key: String, value: V): Long? {
        return redisCache.srem(key, value)
    }

    fun <V : Serializable> smembersTest(redisCache: RedisBinarySet<V>, key: String): List<V> {
        return redisCache.smembers(key)
    }
}