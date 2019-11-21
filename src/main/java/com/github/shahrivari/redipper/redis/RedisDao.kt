package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.api.sync.RedisCommands

class RedisDao(private val config: RedisConfig,
               private val commands: RedisCommands<ByteArray, ByteArray>)
    : RedisCommands<ByteArray, ByteArray> by commands, RedisCacheAPI<ByteArray, ByteArray> {
    private var isClosed = false

    companion object : RedisDaoFactory() {
        @Synchronized
        fun create(config: RedisConfig): RedisDao {
            require(!config.isCluster) { "Config is not set to be in single mode." }
            val connection = create(config.toRedisURI().first(), false)
            val commands = connection.sync()
            return RedisDao(config, commands)
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