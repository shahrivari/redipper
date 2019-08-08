package com.github.shahrivari.redipper.config

import io.lettuce.core.RedisURI
import mu.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

open class RedisConfig {
    var ipList = setOf<String>("127.0.0.1")
    var port: Int = 6379
    var password: String = ""
    var db: Int = 0
    var connectionTimeout: Long = 200
    var readTimeout: Long = 200
    var masterId = ""
    var sentinelPort: Int = 26379


    override fun toString() = "Redis: ${ipList.joinToString(",")}:$port/$db"

    open fun toRedisURI(name: String): RedisURI {
        val redisURI: RedisURI
        // ToDo Amin: can these two be moved to init
        // ToDo Amin: pool
        redisURI = RedisURI.builder()
                .apply {
                    if (password.isNotEmpty())
                        withPassword(password)
                }
                .withDatabase(db)
                // ToDo Amin: read timeout
                .withTimeout(Duration.ofSeconds(connectionTimeout))
                .apply {
                    if (ipList.isEmpty()) {
                        logger.error { "$name Redis ip list is empty" }
                        error("$name redis ip list is empty.")
                    }

                    if (masterId.isEmpty()) {
                        withHost(ipList.first())
                        withPort(port)
                    } else {
                        withSentinelMasterId(masterId)
                        ipList.forEach {
                            withSentinel(it, sentinelPort)
                        }
                    }
                }
                .build()

        return redisURI
    }
}

open class RedisClusterConfig : RedisConfig() {
    override fun toRedisURI(name: String): RedisURI {
        throw
        IllegalStateException("Redis Cluster URIs should be created using toRedisURIs function.")
    }

    fun toRedisURIs(name: String): List<RedisURI> {
        if (ipList.isEmpty()) {
            logger.error { "$name Redis ip list is empty" }
            error("$name redis ip list is empty.")
        }

        return ipList.map {
            RedisURI.builder()
                    .withHost(it)
                    .withPort(port)
                    .apply {
                        if (password.isNotEmpty())
                            withPassword(password)
                    }
                    // ToDo Amin: read timeout
                    .withTimeout(Duration.ofSeconds(connectionTimeout))
                    .build()
        }
    }
}