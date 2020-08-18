package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig

class RedisPublisher(private val config: RedisConfig) {

    private lateinit var redis: RedisPubSubDao

    init {
        createPublisherRedisDao()
    }

    private fun createPublisherRedisDao() {
        redis = RedisPubSubDao.create(config)
    }

    fun publish(topic: String, msg: ByteArray) {
        redis.publish(topic.toByteArray(), msg)
    }
}