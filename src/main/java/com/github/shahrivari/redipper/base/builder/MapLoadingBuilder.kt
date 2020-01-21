package com.github.shahrivari.redipper.base.builder

import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class MapLoadingBuilder<T, V : Serializable>(config: RedisConfig, space: String, clazz: Class<V>) :
        BaseBuilder<T, V>(config, space, clazz) {

    protected var loader: ((String) -> Map<String, V>)? = null

    fun withLoader(loader: (String) -> Map<String, V>): MapLoadingBuilder<T, V> = apply { this.loader = loader }

    override fun withTtl(duration: Long, unit: TimeUnit): MapLoadingBuilder<T, V> =
            super.withTtl(duration, unit) as MapLoadingBuilder<T, V>

    override fun withSerializer(serializer: Serializer<V>): MapLoadingBuilder<T, V> =
            super.withSerializer(serializer) as MapLoadingBuilder<T, V>

    override fun withEncoder(vararg encoder: Encoder): MapLoadingBuilder<T, V> =
            super.withEncoder(*encoder) as MapLoadingBuilder<T, V>
}