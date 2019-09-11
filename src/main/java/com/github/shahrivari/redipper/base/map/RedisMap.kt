package com.github.shahrivari.redipper.base.map

import com.github.shahrivari.redipper.base.LoadingBuilder
import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable

open class RedisMap<V : Serializable> : RedisCache<V> {
    private val loader: ((String) -> V?)?

    protected constructor(config: RedisConfig,
                          loader: ((String) -> V?)?,
                          space: String, ttlSeconds: Long,
                          serializer: Serializer<V>,
                          encoder: Encoder?)
            : super(config, space, ttlSeconds, serializer, encoder) {
        this.loader = loader
    }

    companion object {
        private val EMPTY_BYTES = ByteArray(0)

        inline fun <reified T : Serializable> newBuilder(config: RedisConfig,
                                                         space: String,
                                                         forceSpace: Boolean = false): Builder<T> {
            return Builder(config, space, forceSpace, T::class.java)
        }
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

    fun del(key: String): Long =
            redis.del(key.prependSpace())

    fun removeTtl(key: String) =
            redis.set(key.prependSpace(), redis.get(key.prependSpace()))


    class Builder<V : Serializable>(config: RedisConfig, space: String, forceSpace: Boolean, clazz: Class<V>) :
            LoadingBuilder<RedisMap<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace) checkSpaceExistence(space)
        }

        override fun build(): RedisMap<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()

            return RedisMap(config, loader, space, ttlSeconds, serializer!!, encoder)
        }
    }
}

fun RedisMap<Short>.incr(key: String) =
        redis.incr(key.prependSpace()).toShort()

fun RedisMap<Short>.decr(key: String) =
        redis.decr(key.prependSpace()).toShort()

fun RedisMap<Int>.incr(key: String) =
        redis.incr(key.prependSpace()).toInt()

fun RedisMap<Int>.decr(key: String) =
        redis.decr(key.prependSpace()).toInt()

fun RedisMap<Long>.incr(key: String) =
        redis.incr(key.prependSpace()).toLong()

fun RedisMap<Long>.decr(key: String) =
        redis.decr(key.prependSpace()).toLong()