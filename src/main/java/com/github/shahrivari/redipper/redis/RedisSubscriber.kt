package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.config.RedisConfig

class RedisSubscriber(config: RedisConfig) {
    private var redis: RedisPubSubDao = RedisPubSubDao.create(config)
    private val callbackMap = mutableMapOf<String, (ByteArray) -> Unit>()

    init {
        redis.onMessage { channel, message ->
            val callback = checkNotNull(callbackMap[String(channel)]) {
                "No callback registered for channel $channel in RedisSubscriber."
            }
            callback.invoke(message)
        }
    }


    fun subscribe(topic: String, callback: (ByteArray) -> Unit) {
        redis.subscribe(topic.toByteArray())
        callbackMap[topic] = callback
    }
}