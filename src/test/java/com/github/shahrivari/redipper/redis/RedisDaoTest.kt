package com.github.shahrivari.redipper.redis

import com.github.shahrivari.redipper.base.list.RedisList
import com.github.shahrivari.redipper.base.map.RedisMap
import com.github.shahrivari.redipper.util.RedisCacheTest
import com.github.shahrivari.redipper.util.RedisTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RedisTest::class, RedisCacheTest::class)
internal class RedisDaoTest {
    @Test
    internal fun `close method should work correctly`() {
        var redisList = RedisList.newBuilder<String>(RedisTest.redisConfig, "alaki").build()
        redisList.lpush("1", "1")
        val all = redisList.getAll("1")
        assertThat(all.first()).isEqualTo("1")
        redisList.close()
        val redisMap1 = RedisMap.newBuilder<String>(RedisTest.redisConfig, "map1").build()
        redisMap1.set("1", "1")
        val redisMap2 = RedisMap.newBuilder<String>(RedisTest.redisConfig, "map2").build()
        redisMap1.close()
        redisMap2.set("2", "2")
        val ret = redisMap2.get("2")
        assertThat(ret).isEqualTo("2")
        redisMap2.close()
    }

    @Test
    internal fun `reclose`() {
        var redisList = RedisList.newBuilder<String>(RedisTest.redisConfig, "alaki").build()
        redisList.lpush("1", "1")
        val all = redisList.getAll("1")
        redisList.close()
        assertThrows<IllegalStateException> {
            redisList.close()
        }
        assertThrows<IllegalStateException> {
            redisList.close()
        }
    }
}