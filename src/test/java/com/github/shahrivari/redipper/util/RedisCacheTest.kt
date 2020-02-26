package com.github.shahrivari.redipper.util

import com.github.shahrivari.redipper.base.RedisCache
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.Field
import java.lang.reflect.Modifier


class RedisCacheTest : BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext) {

        // TODO MoHoLiaghat: not work on JDK 12. should be change ASAP
        val field = RedisCache::class.java.getDeclaredField("spaceGroup")
        val modifiersField = Field::class.java.getDeclaredField("modifiers")

        field.isAccessible = true
        modifiersField.isAccessible = true

        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

        field.set(null, HashSet<String>())
    }
}