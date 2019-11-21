package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import com.google.common.collect.HashMultiset
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
import java.util.*

abstract class RedisDaoFactory {
    private val logger = KotlinLogging.logger {}

    companion object {
        protected val clients = Collections.synchronizedMap(mutableMapOf<String, AbstractRedisClient>())
        protected val activeCount = HashMultiset.create<String>()

        fun close(config: RedisConfig) {
            val client = clients[config.uriSignature]
            activeCount.remove(config.uriSignature)
            if (!activeCount.contains(config.uriSignature)) {
                checkNotNull(client) { "Redis client ${config.uriSignature} does not exist." }
                client.shutdown()
                clients.remove(config.uriSignature)
            }
        }

    }

    protected fun create(redisURI: RedisURI,
                         pubsub: Boolean): StatefulRedisConnection<ByteArray, ByteArray> {
        try {
            activeCount.add(redisURI.signature)
            val redisClient =
                    clients.getOrPut(redisURI.signature) {
                        val cli = RedisClient.create(redisURI)
                        // ToDo Amin: fill options
                        cli.options = ClientOptions.builder()
                                .autoReconnect(true)
                                .pingBeforeActivateConnection(true)
                                .build()
                        return@getOrPut cli
                    } as RedisClient

            return if (pubsub)
                redisClient.connectPubSub(ByteArrayCodec())
            else
                redisClient.connect(ByteArrayCodec())

        } catch (e: Throwable) {
            logger.error(e) { "Error in connecting to ${redisURI.signature}" }
            throw IllegalStateException("Error in connecting to ${redisURI.signature}", e)
        }
    }

    protected fun createClusterConnection(redisURIs: List<RedisURI>):
            StatefulRedisClusterConnection<ByteArray, ByteArray> {

        try {
            activeCount.add(redisURIs.signature)
            val redisClient =
                    clients.getOrPut(redisURIs.signature) {
                        val cli = RedisClusterClient.create(redisURIs)
                        // ToDo Amin: fill options
                        cli.setOptions(ClusterClientOptions.builder()
                                               .autoReconnect(true)
                                               .pingBeforeActivateConnection(true)
                                               .build())
                        return@getOrPut cli
                    } as RedisClusterClient

            return redisClient.connect(ByteArrayCodec())
        } catch (e: Throwable) {
            logger.error(e) { "Error in connecting to ${redisURIs.signature}" }
            throw IllegalStateException("Error in connecting to ${redisURIs.signature}", e)
        }
    }
}
