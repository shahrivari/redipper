package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands

class RedisPubSubDao(private val config: RedisConfig,
                     private val commands: RedisPubSubCommands<ByteArray, ByteArray>) :
        RedisPubSubCommands<ByteArray, ByteArray> by commands {
    private var isClosed = false

    private var onMessage: (channel: ByteArray, message: ByteArray) -> Unit = { _, _ -> }

    fun onMessage(callback: (channel: ByteArray, message: ByteArray) -> Unit) {
        onMessage = callback
    }

    companion object : RedisDaoFactory() {
        @Synchronized
        fun create(config: RedisConfig): RedisPubSubDao {
            require(!config.isCluster) { "Config is not set to be in single mode." }
            val connection = create(config.toRedisURI().first(), true) as StatefulRedisPubSubConnection
            val commands = connection.sync() as RedisPubSubCommands
            val redisPubSub = RedisPubSubDao(config, commands)
            val listener = object : RedisPubSubListener<ByteArray, ByteArray> {
                override fun psubscribed(pattern: ByteArray, count: Long) {}

                override fun punsubscribed(pattern: ByteArray, count: Long) {}

                override fun unsubscribed(channel: ByteArray, count: Long) {}

                override fun subscribed(channel: ByteArray, count: Long) {}

                override fun message(pattern: ByteArray, channel: ByteArray, message: ByteArray) {}

                override fun message(channel: ByteArray, message: ByteArray) {
                    redisPubSub.onMessage.invoke(channel, message)
                }
            }
            connection.addListener(listener)
            return redisPubSub
        }
    }

    @Synchronized
    fun close() {
        if (!isClosed) {
            statefulConnection.close()
            RedisDaoFactory.close(config)
            isClosed = true
        } else
            throw IllegalStateException("${config.uriSignature} Already closed!")
    }
}