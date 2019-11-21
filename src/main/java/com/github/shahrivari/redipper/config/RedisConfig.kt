package com.github.shahrivari.redipper.config

import com.github.shahrivari.redipper.redis.signature
import io.lettuce.core.RedisURI
import java.time.Duration

open class RedisConfig {
    var ipList = setOf("127.0.0.1")
    var port: Int = 6379
    var password: String = ""
    var db: Int = 0
    var connectionTimeout: Long = 200
    var masterId = ""
    var sentinelPort: Int = 26379
    var isCluster: Boolean = false

    val uriSignature
        get() =
            if (isCluster) toRedisURI().signature
            else toRedisURI().first().signature

    override fun toString() = "Redis: ${ipList.joinToString(",")}:$port/$db"

    open fun toRedisURI(): List<RedisURI> {
        require(ipList.isNotEmpty()) { "Ip list cannot be empty!" }

        return if (!isCluster) {
            val builder = getUriBuilder().withDatabase(db)
            if (masterId.isEmpty()) {
                builder.withHost(ipList.first()).withPort(port)
            } else {
                builder.withSentinelMasterId(masterId)
                ipList.forEach { builder.withSentinel(it, sentinelPort) }
            }
            listOf(builder.build())

        } else
            ipList.map { getUriBuilder().withHost(it).withPort(port).build() }

    }

    private fun getUriBuilder(): RedisURI.Builder =
            RedisURI.builder().apply {
                if (password.isNotEmpty())
                    withPassword(password)
            }.withTimeout(Duration.ofSeconds(connectionTimeout))
}