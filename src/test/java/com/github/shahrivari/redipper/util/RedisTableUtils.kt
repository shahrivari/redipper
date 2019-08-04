package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.table.RedisTable
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisTableUtils : AppTestUtils, RedisCacheUtils {
    fun <V : Serializable> buildRedisTableTest(space: String,
                                               clazz: Class<V>,
                                               duration: Long = 1,
                                               unit: TimeUnit = TimeUnit.MINUTES): RedisTable<V> {
        return RedisTable.Builder(RedisTest.redisConfig, space, clazz)
                .withTtl(duration, unit)
                .build()
    }

    fun <V : Serializable> hsetTest(redisCache: RedisTable<V>, key: String, field: String, value: V) {
        redisCache.hset(key, field, value)
    }

    fun <V : Serializable> hdelTest(redisCache: RedisTable<V>, key: String, field: String): Long? {
        return redisCache.hdel(key, field)
    }

    fun <V : Serializable> hlenTest(redisCache: RedisTable<V>, key: String, field: String): Long? {
        return redisCache.hlen(key)
    }

    fun <V : Serializable> hexistsTest(redisCache: RedisTable<V>, key: String, field: String): Boolean? {
        return redisCache.hexists(key, field)
    }

    fun <V : Serializable> hgetAllTest(redisCache: RedisTable<V>, key: String, field: String): Map<String, V?> {
        return redisCache.hgetAll(key)
    }
}