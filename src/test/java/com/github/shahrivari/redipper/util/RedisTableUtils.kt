package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.table.RedisTable
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface RedisTableUtils : AppTestUtils, RedisCacheUtils

inline fun <reified V : Serializable> RedisTableUtils.buildRedisTableTest(space: String = randomName,
                                                                          forceSpace: Boolean = false,
                                                                          duration: Long = 1,
                                                                          timeUnit: TimeUnit = TimeUnit.MINUTES,
                                                                          noinline loader: ((String) -> Map<String, V>)? = null,
                                                                          vararg encoder: Encoder)
        : RedisTable<V> {
    val builder =
            RedisTable.newBuilder<V>(RedisTest.redisConfig, space, forceSpace)
                    .withTtl(duration, timeUnit)
                    .withEncoder(*encoder)

    if (loader != null)
        builder.withLoader(loader)

    return builder.build()
}