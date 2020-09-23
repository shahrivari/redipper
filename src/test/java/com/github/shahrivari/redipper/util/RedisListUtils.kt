package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.list.RedisList
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisListUtils : AppTestUtils, RedisCacheUtils

inline fun <reified V : Serializable> RedisListUtils.buildRedisListTest(space: String = randomName,
                                                                        forceSpace: Boolean = false,
                                                                        duration: Long = 1,
                                                                        unit: TimeUnit = TimeUnit.MINUTES,
                                                                        vararg encoder: Encoder)
        : RedisList<V> {
    return RedisList.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
            .withTtl(duration, unit)
            .withEncoder(*encoder)
            .build()
}