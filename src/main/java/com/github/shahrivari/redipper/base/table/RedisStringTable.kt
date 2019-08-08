package com.github.shahrivari.redipper.base.table

import com.github.shahrivari.redipper.base.serialize.StringSerializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.util.concurrent.TimeUnit


class RedisStringTable : RedisTable<String> {

    private constructor(config: RedisConfig, space: String, ttlSeconds: Long)
            : super(config, space, ttlSeconds, StringSerializer())


    class Builder(private val config: RedisConfig, private val space: String) {
        private var ttlSeconds = 0L

        fun withTtl(duration: Long, unit: TimeUnit): Builder {
            require(unit.toSeconds(duration) > 0) { "ttl must be greater than 0!" }
            ttlSeconds = unit.toSeconds(duration)
            return this
        }

        fun build(): RedisStringTable {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            return RedisStringTable(config, space, ttlSeconds)
        }
    }
}