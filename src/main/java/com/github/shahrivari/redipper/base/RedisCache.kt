package com.github.shahrivari.redipper.base

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.map.RedisMap
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import com.github.shahrivari.redipper.redis.RedisCacheAPI
import com.github.shahrivari.redipper.redis.RedisClusterDao
import com.github.shahrivari.redipper.redis.RedisDao
import mu.KotlinLogging
import java.io.Serializable


abstract class RedisCache<V : Serializable> : AutoCloseable {
    protected val space: String
    protected val ttlSeconds: Long
    private val serializer: Serializer<V>
    private val encoder: Encoder?
    internal lateinit var redis: RedisCacheAPI<ByteArray, ByteArray>
    private val config: RedisConfig

    protected constructor(config: RedisConfig,
                          space: String,
                          ttlSeconds: Long,
                          serializer: Serializer<V>,
                          encoder: Encoder?) {
        this.space = space
        this.ttlSeconds = ttlSeconds
        this.serializer = serializer
        this.config = config
        this.encoder = encoder
        spaceGroup.add(space)
        createCacheRedisDao()
    }

    companion object {
        internal val logger = KotlinLogging.logger {}
        private val spaceGroup = HashSet<String>()

        internal fun checkSpaceExistence(space: String) {
            require(!spaceGroup.contains(space))
            { "This space exists:$space. forceSpace should be set true if you want set duplicate space" }
        }

        fun clearWholeRedis(config: RedisConfig) {
            spaceGroup.clear()

            // to clear whole redis
            RedisMap.newBuilder<Int>(config, "reset", true).build().use { it.redis.flushall() }
        }
    }

    private fun createCacheRedisDao() {
        redis = if (config.isCluster)
            RedisClusterDao.create(config)
        else
            RedisDao.create(config)

        logger.info { "Redis $space cache created successfully." }
    }

    protected fun serialize(value: V): ByteArray {
        val serialize = serializer.serialize(value)
        if (encoder != null)
            return encoder.encode(serialize)

        return serialize
    }

    protected fun deserialize(bytes: ByteArray): V? {
        if (bytes.isEmpty()) return null

        var result = bytes
        if (encoder != null)
            result = encoder.decode(bytes)

        return serializer.deserialize(result)
    }

    internal fun getTtl(key: String): Long? = redis.ttl(key.prependSpace())

    internal fun String.prependSpace() = "$space:$this".toByteArray()

    internal fun String.stripSpace() = this.substring("$space:".length)

    override fun close() = redis.close()
}
