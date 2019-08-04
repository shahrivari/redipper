package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig

class RedisSubscriber(private val host: String, private val port: Int = 6379) {
    private lateinit var redis: RedisPubSubDao
    private val callbackMap = mutableMapOf<String, (ByteArray) -> Unit>()

    init {
        createPublisherRedisDao()
        redis.onMessage { channel, message ->
            val callback = checkNotNull(callbackMap[String(channel)]) {
                "No callback registered for channel $channel in RedisSubscriber."
            }
            callback.invoke(message)
        }
    }

    private fun createPublisherRedisDao() {
        redis = RedisPubSubDao.create("Subscriber",
                RedisConfig().apply {
                    ipList = setOf(host)
                    port = this@RedisSubscriber.port
                })
    }

    fun subscribe(topic: String, callback: (ByteArray) -> Unit) {
        redis.subscribe(topic.toByteArray())
        callbackMap[topic] = callback
    }
}