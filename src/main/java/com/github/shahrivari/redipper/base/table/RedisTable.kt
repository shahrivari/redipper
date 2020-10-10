package com.github.shahrivari.redipper.base.table

import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.builder.MapLoadingBuilder
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.ScanArgs
import java.io.Serializable

open class RedisTable<V : Serializable> : RedisCache<V> {
    private val loader: ((String) -> Map<String, V>)?

    protected constructor(config: RedisConfig,
                          loader: ((String) -> Map<String, V>)?,
                          space: String,
                          ttlSeconds: Long,
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

    private fun loadIfNeeded(key: String) {
        if (hlen(key) == 0L && loader != null)
            hmset(key, loader.invoke(key))
    }

    fun hset(key: String, field: String, value: V?) {
        val bytes = if (value == null) EMPTY_BYTES else serialize(value)

        redis.hset(key.prependSpace(), field.toByteArray(), bytes)
        if (ttlSeconds > 0)
            redis.expire(key.prependSpace(), ttlSeconds)
    }

    fun hmset(key: String, fieldValue: Map<String, V?>) {
        if (fieldValue.isEmpty()) return

        val bytes = fieldValue.map {
            it.key.toByteArray() to (it.value?.let { serialize(it) } ?: EMPTY_BYTES)
        }.toMap()

        redis.hmset(key.prependSpace(), bytes)
    }

    fun hget(key: String, field: String): V? {
        loadIfNeeded(key)
        val bytes = redis.hget(key.prependSpace(), field.toByteArray())
        return bytes?.let { deserialize(it) }
    }

    fun hmget(key: String, vararg fields: String): Map<String, V?> {
        loadIfNeeded(key)
        val array = fields.distinct().map { it.toByteArray() }.toTypedArray()
        if (array.isEmpty()) return emptyMap()

        val map = mutableMapOf<String, V?>()

        redis.hmget(key.prependSpace(), *array).forEach {
            //just return the present keys
            val k = String(it.key)
            if (it.hasValue())
                map[k] = deserialize(it.value)
        }

        return map
    }

    fun hgetAll(key: String): Map<String, V?> {
        loadIfNeeded(key)
        return redis.hgetall(key.prependSpace()).map {
            String(it.key) to deserialize(it.value)
        }.toMap()
    }

    fun hdel(key: String, vararg field: String) =
            redis.hdel(key.prependSpace(), *field.map { it.toByteArray() }.toTypedArray())

    fun hexists(key: String, field: String) =
            redis.hexists(key.prependSpace(), field.toByteArray())

    /**
     * returns if key exists in [space]
     */
    fun hexists(key: String) =
            allKeys().contains(key)

    fun hlen(key: String) =
            redis.hlen(key.prependSpace())

    /**
     * returns fields of key.
     */
    fun hkeys(key: String) =
            redis.hkeys(key.prependSpace()).map { String(it) }

    fun del(vararg key: String) =
            redis.del(*key.map { it.prependSpace() }.toTypedArray())

    /**
     * returns all exist keys in [space].
     */
    fun allKeys(limit: Int = 1000, prefix: String = "") =
            redis.scan(ScanArgs().limit(limit.toLong()).match("$space:$prefix*")).keys.map {
                String(it).substringAfter("$space:")
            }


    class Builder<V : Serializable>(config: RedisConfig,
                                    space: String,
                                    forceSpace: Boolean = false,
                                    clazz: Class<V>) :
            MapLoadingBuilder<RedisTable<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace) checkSpaceExistence(space)
        }

        override fun build(): RedisTable<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()

            return RedisTable(config, loader, space, ttlSeconds, serializer!!, encoder)
        }
    }
}

fun <T : Number> RedisTable<T>.incr(key: String): Int = this.redis.incr(key.prependSpace()).toInt()
