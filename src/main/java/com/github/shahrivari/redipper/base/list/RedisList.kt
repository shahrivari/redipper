package com.github.shahrivari.redipper.base.list

import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.serialize.GeneralSerializer
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

open class RedisList<V : Serializable> : RedisCache<V> {

    protected constructor(config: RedisConfig,
                          space: String,
                          ttlSeconds: Long,
                          serializer: Serializer<V>)
            : super(config, space, ttlSeconds, serializer)

    fun lpush(key: String, value: V) {
        redis.lpush(key.prependSpace(), serialize(value))
        if (ttlSeconds > 0)
            redis.expire(key.prependSpace(), ttlSeconds)
    }

    fun lpop(key: String) = deserialize(redis.lpop(key.prependSpace()))

    fun rpop(key: String) = deserialize(redis.rpop(key.prependSpace()))

    fun llen(key: String) = redis.llen(key.prependSpace())

    fun getAll(key: String): List<V?> =
            redis.lrange(key.prependSpace(), 0, -1).map { deserialize(it) }


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

        fun build(): RedisList<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            return RedisList(config, space, ttlSeconds, serializer)
        }
    }
}