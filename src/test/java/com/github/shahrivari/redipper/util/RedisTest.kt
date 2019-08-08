package com.github.shahrivari.redipper.util

import com.github.fppt.jedismock.RedisServer
import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.RedisClient
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext


class RedisTest : BeforeEachCallback {
    companion object {
        private val server: RedisServer = RedisServer.newRedisServer()

        init {
            server.start()
        }

        val redisConfig = RedisConfig().apply {
            ipList = setOf(server.host)
            port = server.bindPort
        }
    }


    override fun beforeEach(context: ExtensionContext) {
        val client = RedisClient.create("redis://${server.host}:${server.bindPort}")
        val connection = client.connect()
        val sync = connection.sync()
        sync.flushall()
        sync.shutdown(false)
    }
}