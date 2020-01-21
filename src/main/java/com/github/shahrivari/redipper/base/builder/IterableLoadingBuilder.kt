package com.github.shahrivari.redipper.base.builder

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class IterableLoadingBuilder<T, V : Serializable>(config: RedisConfig, space: String, clazz: Class<V>) :
        BaseBuilder<T, V>(config, space, clazz) {

    protected var loader: ((String) -> Iterable<V>)? = null

    fun withLoader(loader: (String) -> Iterable<V>): IterableLoadingBuilder<T, V> = apply { this.loader = loader }

    override fun withTtl(duration: Long, unit: TimeUnit): IterableLoadingBuilder<T, V> =
            super.withTtl(duration, unit) as IterableLoadingBuilder<T, V>

    override fun withSerializer(serializer: Serializer<V>): IterableLoadingBuilder<T, V> =
            super.withSerializer(serializer) as IterableLoadingBuilder<T, V>

    override fun withEncoder(vararg encoder: Encoder): IterableLoadingBuilder<T, V> =
            super.withEncoder(*encoder) as IterableLoadingBuilder<T, V>
}