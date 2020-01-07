package com.github.shahrivari.redipper.base

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class MultiLoadingBuilder<T, V : Serializable>(config: RedisConfig, space: String, clazz: Class<V>) :
        BaseBuilder<T, V>(config, space, clazz) {

    protected var loader: ((String) -> Iterable<V>)? = null

    fun withLoader(loader: (String) -> Iterable<V>): MultiLoadingBuilder<T, V> = apply { this.loader = loader }

    override fun withTtl(duration: Long, unit: TimeUnit): MultiLoadingBuilder<T, V> =
            super.withTtl(duration, unit) as MultiLoadingBuilder<T, V>

    override fun withSerializer(serializer: Serializer<V>): MultiLoadingBuilder<T, V> =
            super.withSerializer(serializer) as MultiLoadingBuilder<T, V>

    override fun withEncoder(vararg encoder: Encoder): MultiLoadingBuilder<T, V> =
            super.withEncoder(*encoder) as MultiLoadingBuilder<T, V>
}