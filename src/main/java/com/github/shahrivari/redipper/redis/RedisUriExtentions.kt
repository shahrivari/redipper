package com.github.shahrivari.redipper.redis

import io.lettuce.core.RedisURI

val RedisURI.signature get() = toURI().toASCIIString()

val List<RedisURI>.signature get() = joinToString { it.toURI().toASCIIString() }