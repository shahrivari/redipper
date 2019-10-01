package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.list.RedisList
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisListUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> lpushTest(redisCache: RedisList<V>, key: String, value: V) {
        return redisCache.lpush(key, value)
    }

    fun <V : Serializable> lpopTest(redisCache: RedisList<V>, key: String): V? {
        return redisCache.lpop(key)
    }

    fun <V : Serializable> rpopTest(redisCache: RedisList<V>, key: String): V? {
        return redisCache.rpop(key)
    }

    fun <V : Serializable> llenTest(redisCache: RedisList<V>, key: String): Long {
        return redisCache.llen(key)
    }

    fun <V : Serializable> getAllTest(redisCache: RedisList<V>, key: String): List<V?> {
        return redisCache.getAll(key)
    }
}

inline fun <reified V : Serializable> buildRedisListTest(space: String,
                                                         forceSpace: Boolean = false,
                                                         duration: Long = 1,
                                                         unit: TimeUnit = TimeUnit.MINUTES,
                                                         vararg encoder: Encoder): RedisList<V> {
    return RedisList.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
            .withTtl(duration, unit)
            .withEncoder(*encoder)
            .build()
}