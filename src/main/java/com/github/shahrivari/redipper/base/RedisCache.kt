package com.github.shahrivari.redipper.base

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisClusterConfig
import com.github.shahrivari.redipper.config.RedisConfig
import com.github.shahrivari.redipper.redis.RedisCacheAPI
import com.github.shahrivari.redipper.redis.RedisClusterDao
import com.github.shahrivari.redipper.redis.RedisDao
import mu.KotlinLogging
import java.io.Serializable


abstract class RedisCache<V : Serializable> : AutoCloseable {
    private val space: String
    protected val ttlSeconds: Long
    private val serializer: Serializer<V>
    private val encoder: Encoder?
    protected lateinit var redis: RedisCacheAPI<ByteArray, ByteArray>
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
        createCacheRedisDao()
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

    private fun createCacheRedisDao() {
        if (config is RedisClusterConfig)
            redis = RedisClusterDao.create("Cache", config)
        else
            redis = RedisDao.create("Cache", config)

        logger.info { "Redis $space cache created successfully." }
    }

    protected fun serialize(value: V): ByteArray {
        val serialize = serializer.serialize(value)
        if (encoder != null)
            return encoder.encode(serialize)

        return serialize
    }

    protected fun deserialize(bytes: ByteArray): V? {
        var result = bytes
        if (encoder != null)
            result = encoder.decode(bytes)

        return serializer.deserialize(result)
    }

    protected fun String.prependSpace() = "$space:$this".toByteArray()

    override fun close() = redis.close()
}
