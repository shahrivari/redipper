package com.github.shahrivari.redipper.redis

import io.lettuce.core.cluster.api.sync.RedisClusterCommands

interface RedisCacheAPI<K, V> : RedisClusterCommands<K, V> {
    fun close()
}