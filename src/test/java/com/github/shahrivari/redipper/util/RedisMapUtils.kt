package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.map.RedisMap
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisMapUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> setTest(redisCache: RedisMap<V>, key: String, value: V?) =
            redisCache.set(key, value)

    fun <V : Serializable> msetTest(redisCache: RedisMap<V>, kvs: Map<String, V?>) =
            redisCache.mset(kvs)

    fun <V : Serializable> delTest(redisCache: RedisMap<V>, key: String): Long? =
            redisCache.del(key)

    fun <V : Serializable> getTest(redisCache: RedisMap<V>, key: String): V? =
            redisCache.get(key)

    fun <V : Serializable> mgetTest(redisCache: RedisMap<V>, keys: Iterable<String>) =
            redisCache.mget(keys)
}

inline fun <reified V : Serializable> RedisMapUtils.buildRedisMapTest(space: String = randomName,
                                                                      forceSpace: Boolean = false,
                                                                      duration: Long = 1,
                                                                      unit: TimeUnit = TimeUnit.MINUTES,
                                                                      noinline loader: ((String) -> V?)? = null,
                                                                      vararg encoder: Encoder): RedisMap<V> {
    val builder =
            RedisMap.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
                    .withTtl(duration, unit)
                    .withEncoder(*encoder)

    if (loader != null)
        builder.withLoader(loader)

    return builder.build()
}