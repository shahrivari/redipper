package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig

class RedisPublisher(private val host: String, private val port: Int) {

    private lateinit var redis: RedisPubSubDao

    init {
        createPublisherRedisDao()
    }

    private fun createPublisherRedisDao() {
        redis = RedisPubSubDao.create("Publisher",
                RedisConfig().apply {
                    ipList = setOf(host)
                    port = this@RedisPublisher.port
                })
    }

    fun publish(topic: String, msg: ByteArray) {
        redis.publish(topic.toByteArray(), msg)
    }
}