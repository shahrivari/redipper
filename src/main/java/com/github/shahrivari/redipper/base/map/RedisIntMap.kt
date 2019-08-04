package com.github.shahrivari.redipper.base.map

import com.github.shahrivari.redipper.base.serialize.IntSerializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.util.concurrent.TimeUnit

class RedisIntMap : RedisMap<Int> {

    private constructor(config: RedisConfig,
                        loader: ((String) -> Int?)?,
                        space: String,
                        ttlSeconds: Long)
            : super(config, loader, space, ttlSeconds, IntSerializer())

    fun incr(key: String): Int = redis.incr(key.prependSpace()).toInt()

    fun decr(key: String): Int = redis.decr(key.prependSpace()).toInt()


    class Builder(private val config: RedisConfig, private val space: String) {
        private var ttlSeconds = 0L
        private var loader: ((String) -> Int?)? = null

        fun withTtl(duration: Long, unit: TimeUnit): Builder {
            require(unit.toSeconds(duration) > 0) { "ttl must be greater than 0!" }
            ttlSeconds = unit.toSeconds(duration)
            return this
        }

        fun withLoader(loader: (String) -> Int?): Builder {
            this.loader = loader
            return this
        }

        fun build(): RedisIntMap {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            return RedisIntMap(config, loader, space, ttlSeconds)
        }
    }
}