package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.map.RedisMap
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisMapUtils : AppTestUtils, RedisCacheUtils

inline fun <reified V : Serializable> RedisMapUtils.buildRedisMapTest(space: String = randomName,
                                                                      forceSpace: Boolean = false,
                                                                      duration: Long = 1,
                                                                      unit: TimeUnit = TimeUnit.MINUTES,
                                                                      noinline loader: ((String) -> V?)? = null,
                                                                      vararg encoder: Encoder)
        : RedisMap<V> {
    val builder =
            RedisMap.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
                    .withTtl(duration, unit)
                    .withEncoder(*encoder)

    if (loader != null)
        builder.withLoader(loader)

    return builder.build()
}