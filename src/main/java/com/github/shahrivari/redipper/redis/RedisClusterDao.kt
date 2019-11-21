package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands

class RedisClusterDao(private val config: RedisConfig,
                      private val commands: RedisAdvancedClusterCommands<ByteArray, ByteArray>)
    : RedisAdvancedClusterCommands<ByteArray, ByteArray> by commands, RedisCacheAPI<ByteArray, ByteArray> {
    private var isClosed = false

    companion object : RedisDaoFactory() {
        @Synchronized
        fun create(config: RedisConfig): RedisClusterDao {
            require(config.isCluster) { "Config is not set to be in cluster mode." }
            val connection = createClusterConnection(config.toRedisURI())
            val commands = connection.sync()
            return RedisClusterDao(config, commands)
        }
    }

    @Synchronized
    override fun close() {
        if (!isClosed) {
            statefulConnection.close()
            RedisDaoFactory.close(config)
            isClosed = true
        } else
            throw IllegalStateException("${config.uriSignature} Already closed!")
    }
}