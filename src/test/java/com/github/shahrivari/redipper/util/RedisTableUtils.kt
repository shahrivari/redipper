package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.table.RedisTable
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisTableUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> hsetTest(redisCache: RedisTable<V>, key: String, field: String, value: V) =
            redisCache.hset(key, field, value)

    fun <V : Serializable> hdelTest(redisCache: RedisTable<V>, key: String, field: String): Long? =
            redisCache.hdel(key, field)

    fun <V : Serializable> hlenTest(redisCache: RedisTable<V>, key: String, field: String): Long? =
            redisCache.hlen(key)

    fun <V : Serializable> hexistsTest(redisCache: RedisTable<V>, key: String, field: String): Boolean? =
            redisCache.hexists(key, field)

    fun <V : Serializable> hgetAllTest(redisCache: RedisTable<V>, key: String, field: String): Map<String, V?> =
            redisCache.hgetAll(key)
}

inline fun <reified V : Serializable> RedisTableUtils.buildRedisTableTest(space: String = randomName,
                                                                          forceSpace: Boolean = false,
                                                                          duration: Long = 1,
                                                                          unit: TimeUnit = TimeUnit.MINUTES,
                                                                          vararg encoder: Encoder): RedisTable<V> {
    return RedisTable.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
            .withTtl(duration, unit)
            .withEncoder(*encoder)
            .build()
}