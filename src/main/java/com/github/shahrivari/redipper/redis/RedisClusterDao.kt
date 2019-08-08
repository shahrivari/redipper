package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisClusterConfig
import io.lettuce.core.RedisURI
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands

class RedisClusterDao(private val name: String,
                      private val commands: RedisAdvancedClusterCommands<ByteArray, ByteArray>)
    : RedisAdvancedClusterCommands<ByteArray, ByteArray> by commands, RedisCacheAPI<ByteArray, ByteArray> {

    companion object : RedisDaoFactory() {
        @Synchronized
        fun create(name: String, config: RedisClusterConfig): RedisClusterDao {
            return create(name, config.toRedisURIs(name))
        }

        @Synchronized
        fun create(name: String, redisURIs: List<RedisURI>): RedisClusterDao {
            val connection = createClusterConnection(name, redisURIs)
            val commands = connection.sync()
            return RedisClusterDao(name, commands)
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