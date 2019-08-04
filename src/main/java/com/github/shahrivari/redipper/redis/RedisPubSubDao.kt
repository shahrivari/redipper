package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands

class RedisPubSubDao(private val name: String,
                     private val commands: RedisPubSubCommands<ByteArray, ByteArray>) :
        RedisPubSubCommands<ByteArray, ByteArray> by commands {

    private var onMessage: (channel: ByteArray, message: ByteArray) -> Unit = { _, _ -> }

    fun onMessage(callback: (channel: ByteArray, message: ByteArray) -> Unit) {
        onMessage = callback
    }

    companion object : RedisDaoFactory() {
        @Synchronized
        fun create(name: String, config: RedisConfig): RedisPubSubDao {
            val connection = create(name, config, true) as StatefulRedisPubSubConnection
            val commands = connection.sync() as RedisPubSubCommands
            val redisPubSub = RedisPubSubDao(name, commands)
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
        statefulConnection.close()
        val client = clients[name]
        checkNotNull(client) { "Redis client $name does not exist." }
        client.shutdown()
        clients.remove(name)
    }
}