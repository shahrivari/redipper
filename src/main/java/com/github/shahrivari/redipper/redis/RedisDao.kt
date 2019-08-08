package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.RedisURI
import io.lettuce.core.api.sync.RedisCommands

class RedisDao(private val name: String,
               private val commands: RedisCommands<ByteArray, ByteArray>)
    : RedisCommands<ByteArray, ByteArray> by commands, RedisCacheAPI<ByteArray, ByteArray> {

    companion object : RedisDaoFactory() {
        @Synchronized
        fun create(name: String, config: RedisConfig): RedisDao {
            val connection = create(name, config, false)
            val commands = connection.sync()
            return RedisDao(name, commands)
        }

        @Synchronized
        fun create(name: String, redisURI: RedisURI): RedisDao {
            val connection = create(name, redisURI, false)
            val commands = connection.sync()
            return RedisDao(name, commands)
        }
    }

    @Synchronized
    override fun close() {
        statefulConnection.close()
        val client = clients[name]
        checkNotNull(client) { "Redis client $name does not exist." }
        client.shutdown()
        clients.remove(name)
    }
}