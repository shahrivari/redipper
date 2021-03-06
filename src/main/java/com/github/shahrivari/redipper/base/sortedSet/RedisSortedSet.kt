package com.github.shahrivari.redipper.base.sortedSet

import com.github.shahrivari.redipper.base.RedisCache
import com.github.shahrivari.redipper.base.builder.BaseBuilder
import com.github.shahrivari.redipper.base.encoding.Encoder
import com.github.shahrivari.redipper.base.serialize.Serializer
import com.github.shahrivari.redipper.config.RedisConfig
import io.lettuce.core.Limit
import io.lettuce.core.Range
import io.lettuce.core.ScoredValue
import java.io.Serializable

open class RedisSortedSet<V : Serializable> : RedisCache<V> {

    protected constructor(config: RedisConfig,
                          space: String,
                          ttlSeconds: Long,
                          serializer: Serializer<V>,
                          encoder: Encoder?)
            : super(config, space, ttlSeconds, serializer, encoder)

    companion object {
        inline fun <reified T : Serializable> newBuilder(config: RedisConfig,
                                                         space: String,
                                                         forceSpace: Boolean = false): Builder<T> {
            return Builder(config, space, forceSpace, T::class.java)
        }
    }

    /**
     * @param values first is priority of value, second is value.
     */
    fun zadd(key: String, vararg values: Pair<Double, V>) {
        val scoredValues = values.map {
            ScoredValue.just(it.first, serialize(it.second))
        }

        redis.zadd(key.prependSpace(), *scoredValues.toTypedArray())
        if (ttlSeconds > 0)
            redis.expire(key.prependSpace(), ttlSeconds)
    }

    /**
     * if [start] == 0 and [stop] == -1
     * then returns all values of key
     */
    fun zrange(key: String, start: Int, stop: Int): List<V?> {
        val serializedDataList = redis.zrange(key.prependSpace(), start.toLong(), stop.toLong())
        return serializedDataList.map { deserialize(it) }
    }

    fun zrangeByScore(key: String, min: Int = 0, max: Int = -1, limit: Int): List<V?> {
        // returns all values of key
        if (min == 0 && max == -1)
            return zrange(key, 0, -1)

        if (min > max) {
            logger.error { "Min should not be greater than max: $min > $max incorrect." }
            return listOf()
        }

        val list = redis.zrangebyscore(key.prependSpace(),
                                       Range.create(min, max),
                                       Limit.from(limit.toLong()))

        return list.mapNotNull { deserialize(it) }
    }

    fun zrem(key: String, value: V): Long =
            redis.zrem(key.prependSpace(), serialize(value))

    fun zremrangeByScore(key: String, score: Int): Long =
            redis.zremrangebyscore(key.prependSpace(), Range.create(score, score))

    fun zremrangebyscore(key: String, minScore: Double, maxScore: Double) =
            redis.zremrangebyscore(key.prependSpace(), Range.create(minScore, maxScore))

    fun zcard(key: String) =
            redis.zcard(key.prependSpace())

    fun zrevrange(key: String, start: Long, stop: Long) =
            redis.zrevrangeWithScores(key.prependSpace(), start, stop)
                    .mapNotNull { it.value?.let { _ -> it.score to deserialize(it.value) } }
                    .toMap()

    fun del(vararg key: String) =
            redis.del(*key.map { it.prependSpace() }.toTypedArray())

    class Builder<V : Serializable>(config: RedisConfig, space: String, forceSpace: Boolean, clazz: Class<V>) :
            BaseBuilder<RedisSortedSet<V>, V>(config, space, clazz) {

        init {
            if (!forceSpace) checkSpaceExistence(space)
        }

        override fun build(): RedisSortedSet<V> {
            require(!space.contains(":")) { "space cannot have semicolon: $space" }
            if (serializer == null)
                specifySerializer()

            return RedisSortedSet(config, space, ttlSeconds, serializer!!, encoder)
        }
    }
}