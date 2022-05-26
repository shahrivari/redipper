package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig

class RedisPublisher(config: RedisConfig) {

    private val redis: RedisPubSubDao = RedisPubSubDao.create(config)


    fun publish(topic: String, msg: ByteArray) {
        redis.publish(topic.toByteArray(), msg)
    }
}