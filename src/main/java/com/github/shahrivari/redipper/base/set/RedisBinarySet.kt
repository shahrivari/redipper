package com.github.shahrivari.redipper.base.set

import com.github.shahrivari.redipper.base.BaseBuilder
import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable

open class RedisBinarySet<V : Serializable> : RedisCache<V> {

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

    fun sadd(key: String, value: V) {
        redis.sadd(key.prependSpace(), serialize(value))
        if (ttlSeconds > 0)
            redis.expire(key.prependSpace(), ttlSeconds)
    }

    fun srem(key: String, value: V) = redis.srem(key.prependSpace(), serialize(value))

    fun smembers(key: String) =
            redis.smembers(key.prependSpace()).mapNotNull { deserialize(it) }


    class Builder<V : Serializable>(config: RedisConfig, space: String, forceSpace: Boolean, clazz: Class<V>) :
            BaseBuilder<RedisBinarySet<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace) checkSpaceExistence(space)
        }

        override fun build(): RedisBinarySet<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()

            return RedisBinarySet(config, space, ttlSeconds, serializer!!, encoder)
        }
    }
}