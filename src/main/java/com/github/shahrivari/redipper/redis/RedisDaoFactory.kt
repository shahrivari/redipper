package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.AbstractRedisClient
import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.codec.ByteArrayCodec
import mu.KotlinLogging

abstract class RedisDaoFactory {
    private val logger = KotlinLogging.logger {}

    protected val clients = mutableMapOf<String, RedisClient>()

    protected fun create(name: String,
                         config: RedisConfig,
                         pubsub: Boolean): StatefulRedisConnection<ByteArray, ByteArray> {
        require(!config.isCluster) { "Config is not set to be in single mode." }
        val redisURI = config.toRedisURI(name).first()
        return create(name, redisURI, pubsub)
    }

    protected fun create(name: String,
                         redisURI: RedisURI,
                         pubsub: Boolean): StatefulRedisConnection<ByteArray, ByteArray> {
        // ToDo Amin: can these two be moved to init
        val client: RedisClient

        try {
            if (clients.containsKey(name))
                error("$name redis already exists.")

            // ToDo Amin: pool
            client = RedisClient.create(redisURI)
            // ToDo Amin: fill options
            client.options = ClientOptions.builder()
                    .autoReconnect(true)
                    .pingBeforeActivateConnection(true)
                    .build()

            return if (pubsub)
                client.connectPubSub(ByteArrayCodec())
            else
                client.connect(ByteArrayCodec())

        } catch (e: Throwable) {
            logger.error(e) { "Error in connecting to ${name}Redis." }
            throw IllegalStateException("Error in connecting to ${name}Redis.", e)
        }
    }

    protected fun createClusterConnection(name: String, redisURIs: List<RedisURI>):
            StatefulRedisClusterConnection<ByteArray, ByteArray> {
        val client: AbstractRedisClient

        try {
            if (clients.containsKey(name))
                error("$name redis already exists.")

            // ToDo Amin: pool
            client = RedisClusterClient.create(redisURIs)
            // ToDo Amin: fill options
            client.setOptions(ClusterClientOptions.builder()
                                      .autoReconnect(true)
                                      .pingBeforeActivateConnection(true)
                                      .build())

            return client.connect(ByteArrayCodec())
        } catch (e: Throwable) {
            logger.error(e) { "Error in connecting to ${name}Redis." }
            throw IllegalStateException("Error in connecting to ${name}Redis.", e)
        }
    }
}
