package com.github.shahrivari.redipper.base.builder

import com.github.shahrivari.redipper.base.encoding.CombinedEncoder
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.*
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class BaseBuilder<T, V : Serializable>(protected val config: RedisConfig,
                                                protected val space: String,
                                                private val clazz: Class<V>) {
    protected var ttlSeconds: Long = 0L
    protected var serializer: Serializer<V>? = null
    protected var encoder: Encoder? = null

    open fun withTtl(duration: Long, unit: TimeUnit): BaseBuilder<T, V> {
        require(unit.toSeconds(duration) > 0) { "ttl must be greater than 0!" }
        ttlSeconds = unit.toSeconds(duration)
        return this
    }

    open fun withSerializer(serializer: Serializer<V>): BaseBuilder<T, V> {
        this.serializer = serializer
        return this
    }

    open fun withEncoder(vararg encoder: Encoder): BaseBuilder<T, V> {
        this.encoder = CombinedEncoder.of(*encoder)
        return this
    }

    protected fun specifySerializer() {
        serializer = when (clazz) {
            Integer::class.java -> IntSerializer() as Serializer<V>
            java.lang.Long::class.java -> LongSerializer() as Serializer<V>
            String::class.java -> StringSerializer() as Serializer<V>
            ByteArray::class.java -> ByteArraySerializer() as Serializer<V>
            else -> GeneralSerializer(clazz)
        }
    }

    abstract fun build(): T
}