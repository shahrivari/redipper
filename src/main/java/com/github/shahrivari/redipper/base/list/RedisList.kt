package com.github.shahrivari.redipper.base.list

import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.builder.BaseBuilder
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable

open class RedisList<V : Serializable> : RedisCache<V> {

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


    class Builder<V : Serializable>(config: RedisConfig, space: String, forceSpace: Boolean, clazz: Class<V>) :
            BaseBuilder<RedisList<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace)
                checkSpaceExistence(space)
        }

        override fun build(): RedisList<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()
            return RedisList(config, space, ttlSeconds, serializer!!, encoder)
        }
    }
}