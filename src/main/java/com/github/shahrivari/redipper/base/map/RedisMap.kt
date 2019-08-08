package com.github.shahrivari.redipper.base.map

import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.serialize.GeneralSerializer
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

open class RedisMap<V : Serializable> : RedisCache<V> {
    private val loader: ((String) -> V?)?

    protected constructor(config: RedisConfig,
                          loader: ((String) -> V?)?,
                          space: String, ttlSeconds: Long,
                          serializer: Serializer<V>)
            : super(config, space, ttlSeconds, serializer) {
        this.loader = loader
    }

    companion object {
        private val EMPTY_BYTES = ByteArray(0)
    }

    private fun ByteArray.getKeyPart(): String {
        val str = String(this)
        return str.substring(str.indexOf(':') + 1)
    }

    private fun loadIfNeeded(key: String, value: ByteArray?): V? {
        return when {
            value == null -> {
                if (loader != null) {
                    val result = loader.invoke(key)
                    set(key, result)
                    result
                } else {
                    null
                }
            }
            value.isEmpty() -> null
            else -> deserialize(value)
        }
    }

    fun set(key: String, value: V?) {
        val bytes = if (value == null) EMPTY_BYTES else serialize(value)
        if (ttlSeconds > 0)
            redis.setex(key.prependSpace(), ttlSeconds, bytes)
        else
            redis.set(key.prependSpace(), bytes)
    }

    fun get(key: String): V? {
        val bytes = redis.get(key.prependSpace())
        return loadIfNeeded(key, bytes)
    }

    // ToDo MoHoLiaghat: ba for khoob nist bayad avaz she -> batch beshe
    fun mset(kvs: Map<String, V>) =
            kvs.entries.parallelStream().forEach { (k, v) -> set(k, v) }

    fun mget(keys: Iterable<String>): Map<String, V> {
        val array = keys.distinct().map { it.prependSpace() }.toTypedArray()
        if (array.isEmpty()) return emptyMap()

        val map = mutableMapOf<String, V>()

        redis.mget(*array).forEach {
            //just return the present keys
            val k = it.key.getKeyPart()
            val v = if (it.hasValue()) deserialize(it.value) else loadIfNeeded(k, null)
            if (v != null) map[k] = v
        }

        return map
    }

    fun del(key: String) = redis.del(key.prependSpace())

    fun removeTtl(key: String) = redis.set(key.prependSpace(), redis.get(key.prependSpace()))


    class Builder<V : Serializable>(private val config: RedisConfig,
                                    private val space: String,
                                    clazz: Class<V>) {
        private var ttlSeconds: Long = 0L
        private var loader: ((String) -> V?)? = null
        private var serializer: Serializer<V> = GeneralSerializer(clazz)

        fun withTtl(duration: Long, unit: TimeUnit): Builder<V> {
            require(unit.toSeconds(duration) > 0) { "ttl must be greater than 0!" }
            ttlSeconds = unit.toSeconds(duration)
            return this
        }

        fun withLoader(loader: (String) -> V?): Builder<V> {
            this.loader = loader
            return this
        }

        fun withSerializer(serializer: Serializer<V>): Builder<V> {
            this.serializer = serializer
            return this
        }

        fun build(): RedisMap<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            return RedisMap(config, loader, space, ttlSeconds, serializer)
        }
    }
}