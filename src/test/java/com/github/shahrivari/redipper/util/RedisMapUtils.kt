package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.map.RedisMap
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisMapUtils : AppTestUtils, RedisCacheUtils {
    fun <V : Serializable> buildRedisMapTest(space: String,
                                             clazz: Class<V>,
                                             duration: Long = 1,
                                             unit: TimeUnit = TimeUnit.MINUTES,
                                             l: ((String) -> V?)? = null,
                                             encoder: Encoder? = null): RedisMap<V> {
        return if (l == null && encoder == null)
            RedisMap.Builder(RedisTest.redisConfig, space, clazz)
                    .withTtl(duration, unit)
                    .build()
        else if (l != null && encoder == null)
            RedisMap.Builder(RedisTest.redisConfig, space, clazz)
                    .withTtl(duration, unit)
                    .withLoader(l)
                    .build()
        else if (l == null && encoder != null)
            RedisMap.Builder(RedisTest.redisConfig, space, clazz)
                    .withTtl(duration, unit)
                    .withEncoder(encoder)
                    .build()
        else
            RedisMap.Builder(RedisTest.redisConfig, space, clazz)
                    .withTtl(duration, unit)
                    .withLoader(l!!)
                    .withEncoder(encoder!!)
                    .build()
    }

    fun <V : Serializable> setTest(redisCache: RedisMap<V>, key: String, value: V?) {
        redisCache.set(key, value)
    }

    fun <V : Serializable> msetTest(redisCache: RedisMap<V>, kvs: Map<String, V>) {
        redisCache.mset(kvs)
    }

    fun <V : Serializable> delTest(redisCache: RedisMap<V>, key: String): Long? {
        return redisCache.del(key)
    }

    fun <V : Serializable> getTest(redisCache: RedisMap<V>, key: String): V? {
        return redisCache.get(key)
    }

    fun <V : Serializable> mgetTest(redisCache: RedisMap<V>, keys: Iterable<String>): Map<String, V> {
        return redisCache.mget(keys)
    }
}