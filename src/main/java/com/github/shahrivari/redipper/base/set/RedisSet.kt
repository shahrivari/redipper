package com.github.shahrivari.redipper.base.set

import com.github.shahrivari.redipper.base.MultiLoadingBuilder
import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable

open class RedisSet<V : Serializable> : RedisCache<V> {
    private val loader: ((String) -> Iterable<V>)?

    protected constructor(config: RedisConfig,
                          loader: ((String) -> Iterable<V>)?,
                          space: String,
                          ttlSeconds: Long,
                          serializer: Serializer<V>,
                          encoder: Encoder?)
            : super(config, space, ttlSeconds, serializer, encoder) {
        this.loader = loader
    }

    companion object {
        inline fun <reified T : Serializable> newBuilder(config: RedisConfig,
                                                         space: String,
                                                         forceSpace: Boolean = false): Builder<T> {
            return Builder(config, space, forceSpace, T::class.java)
        }
    }

    private fun loadIfNeeded(key: String, value: MutableSet<ByteArray>): Set<V> {
        if (value.isEmpty())
            if (loader != null) {
                val result = loader.invoke(key).toSet()
                result.forEach { sadd(key, it) }
                return result
            }
        return value.mapNotNull { deserialize(it) }.toSet()
    }

    fun sadd(key: String, vararg value: V) {
        redis.sadd(key.prependSpace(), *value.map { serialize(it) }.toTypedArray())
        if (ttlSeconds > 0)
            redis.expire(key.prependSpace(), ttlSeconds)
    }

    fun srem(key: String, vararg value: V) =
            redis.srem(key.prependSpace(), *value.map { serialize(it) }.toTypedArray())

    fun smembers(key: String): Set<V> =
            loadIfNeeded(key, redis.smembers(key.prependSpace()))

    fun isMember(key: String, value: V) =
            redis.sismember(key.prependSpace(), serialize(value))


    class Builder<V : Serializable>(config: RedisConfig, space: String, forceSpace: Boolean, clazz: Class<V>) :
            MultiLoadingBuilder<RedisSet<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace) checkSpaceExistence(space)
        }

        override fun build(): RedisSet<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()

            return RedisSet(config, loader, space, ttlSeconds, serializer!!, encoder)
        }
    }
}