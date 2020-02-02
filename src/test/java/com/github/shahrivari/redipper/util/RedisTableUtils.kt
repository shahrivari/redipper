package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.table.RedisTable
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisTableUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> hsetTest(redisCache: RedisTable<V>, key: String, field: String, value: V?) =
            redisCache.hset(key, field, value)

    fun <V : Serializable> hmsetTest(redisCache: RedisTable<V>, key: String, fieldValue: Map<String, V?>) =
            redisCache.hmset(key, fieldValue)

    fun <V : Serializable> hdelTest(redisCache: RedisTable<V>, key: String, vararg field: String) =
            redisCache.hdel(key, *field)

    fun <V : Serializable> hlenTest(redisCache: RedisTable<V>, key: String, field: String): Long? =
            redisCache.hlen(key)

    fun <V : Serializable> hexistsTest(redisCache: RedisTable<V>, key: String, field: String): Boolean? =
            redisCache.hexists(key, field)

    fun <V : Serializable> hgetTest(redisCache: RedisTable<V>, key: String, field: String) =
            redisCache.hget(key, field)

    fun <V : Serializable> hmgetTest(redisCache: RedisTable<V>, key: String, vararg field: String) =
            redisCache.hmget(key, *field)

    fun <V : Serializable> hgetAllTest(redisCache: RedisTable<V>, key: String) =
            redisCache.hgetAll(key)

    fun <V : Serializable> hkeysAllTest(redisCache: RedisTable<V>, key: String) =
            redisCache.hkeys(key)
}

inline fun <reified V : Serializable> RedisTableUtils.buildRedisTableTest(space: String = randomName,
                                                                          forceSpace: Boolean = false,
                                                                          duration: Long = 1,
                                                                          timeUnit: TimeUnit = TimeUnit.MINUTES,
                                                                          noinline loader: ((String) -> Map<String, V>)? = null,
                                                                          vararg encoder: Encoder): RedisTable<V> {
    val builder =
            RedisTable.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
                    .withTtl(duration, timeUnit)
                    .withEncoder(*encoder)

    if (loader != null)
        builder.withLoader(loader)

    return builder.build()
}