package com.github.shahrivari.redipper.base

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.map.RedisMap
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class LoadingBuilder<T, V : Serializable>(config: RedisConfig, space: String, clazz: Class<V>) :
        BaseBuilder<RedisMap<V>, V>(config, space, clazz) {

    protected var loader: ((String) -> V?)? = null

    fun withLoader(loader: (String) -> V?): LoadingBuilder<T, V> = apply { this.loader = loader }

    override fun withTtl(duration: Long, unit: TimeUnit): LoadingBuilder<T, V> =
            super.withTtl(duration, unit) as LoadingBuilder<T, V>

    override fun withSerializer(serializer: Serializer<V>): LoadingBuilder<T, V> =
            super.withSerializer(serializer) as LoadingBuilder<T, V>

    override fun withEncoder(vararg encoder: Encoder): LoadingBuilder<T, V> =
            super.withEncoder(*encoder) as LoadingBuilder<T, V>
}