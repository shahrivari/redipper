package com.github.shahrivari.redipper.util

import com.github.fppt.jedismock.RedisServer
import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext


object RedisTest : BeforeEachCallback {
    private val server: RedisServer = RedisServer.newRedisServer()
    private val client: RedisClient
    private val connection: RedisCommands<String, String>
    val redisConfig: RedisConfig

    init {
        server.start()

        redisConfig = RedisConfig().apply {
            ipList = setOf(server.host)
            port = server.bindPort
        }

        client = RedisClient.create("redis://${server.host}:${server.bindPort}")

        connection = client.connect().sync()
    }

    override fun beforeEach(context: ExtensionContext) {
        connection.apply {
            flushall()
            shutdown(false)
        }
    }
}
