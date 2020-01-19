package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.set.RedisSet
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisSetUtils : AppTestUtils, RedisCacheUtils {

    fun <V : Serializable> saddTest(redisCache: RedisSet<V>, key: String, value: V) =
            redisCache.sadd(key, value)

    fun <V : Serializable> sremTest(redisCache: RedisSet<V>, key: String, value: V): Long? =
            redisCache.srem(key, value)

    fun <V : Serializable> smembersTest(redisCache: RedisSet<V>, key: String): List<V> =
            redisCache.smembers(key).toList()

    fun <V : Serializable> delTest(redisCache: RedisSet<V>, key: String): Long? =
            redisCache.del(key)
}

inline fun <reified V : Serializable> RedisSetUtils.buildRedisSetTest(space: String = randomName,
                                                                      forceSpace: Boolean = false,
                                                                      duration: Long = 1,
                                                                      unit: TimeUnit = TimeUnit.MINUTES,
                                                                      vararg encoder: Encoder,
                                                                      noinline loader: ((String) -> Iterable<V>)? = null): RedisSet<V> {
    val builder =
            RedisSet.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
                    .withTtl(duration, unit)
                    .withEncoder(*encoder)

    if (loader != null)
        builder.withLoader(loader)

    return builder.build()
}