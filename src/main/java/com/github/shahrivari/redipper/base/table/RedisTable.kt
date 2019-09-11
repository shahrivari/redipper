package com.github.shahrivari.redipper.base.table

import com.github.shahrivari.redipper.base.BaseBuilder
import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable

open class RedisTable<V : Serializable> : RedisCache<V> {

    protected constructor(config: RedisConfig,
                          space: String,
                          ttlSeconds: Long,
                          serializer: Serializer<V>,
                          encoder: Encoder?)
            : super(config, space, ttlSeconds, serializer, encoder)

    companion object {
        inline fun <reified T : Serializable> newBuilder(config: RedisConfig,
                                                         space: String,
                                                         forceSpace: Boolean = false): Builder<T> {
            return Builder(config, space, forceSpace, T::class.java)
        }
    }

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


    class Builder<V : Serializable>(config: RedisConfig, space: String, forceSpace: Boolean = false, clazz: Class<V>) :
            BaseBuilder<RedisTable<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace) checkSpaceExistence(space)
        }

        override fun build(): RedisTable<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()

            return RedisTable(config, space, ttlSeconds, serializer!!, encoder)
        }
    }
}

fun <T : Number> RedisTable<T>.incr(key: String): Int = this.redis.incr(key.prependSpace()).toInt()
