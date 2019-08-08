package com.github.shahrivari.redipper.base.table

import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.serialize.GeneralSerializer
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

open class RedisTable<V : Serializable> : RedisCache<V> {

    protected constructor(config: RedisConfig,
                          space: String,
                          ttlSeconds: Long,
                          serializer: Serializer<V>)
            : super(config, space, ttlSeconds, serializer)

    fun hset(key: String, field: String, value: V) {
        redis.hset(key.prependSpace(), field.toByteArray(), serialize(value))
        if (ttlSeconds > 0)
            redis.expire(key.prependSpace(), ttlSeconds)
    }

    fun hdel(key: String, field: String) =
            redis.hdel(key.prependSpace(), field.toByteArray())

    fun hgetAll(key: String) =
            redis.hgetall(key.prependSpace())
                    .map { String(it.key, Charsets.UTF_8) to deserialize(it.value) }.toMap()

    fun hexists(key: String, field: String) =
            redis.hexists(key.prependSpace(), field.toByteArray())

    fun hlen(key: String) = redis.hlen(key.prependSpace())


    class Builder<V : Serializable>(private val config: RedisConfig,
                                    private val space: String,
                                    clazz: Class<V>) {
        private var ttlSeconds: Long = 0L
        private var serializer: Serializer<V> = GeneralSerializer(clazz)

        fun withTtl(duration: Long, unit: TimeUnit): Builder<V> {
            require(unit.toSeconds(duration) > 0) { "ttl must be greater than 0!" }
            ttlSeconds = unit.toSeconds(duration)
            return this
        }

        fun withSerializer(serializer: Serializer<V>): Builder<V> {
            this.serializer = serializer
            return this
        }

        fun build(): RedisTable<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            return RedisTable(config, space, ttlSeconds, serializer)
        }
    }
}