package com.github.shahrivari.redipper.util

import java.io.Serializable

interface RedisCacheUtils : AppTestUtils {

    data class Person(val name: String, val id: Int, val phone: String) : Serializable {
        companion object {
            @JvmStatic
            private val serialVersionUID: Long = 6587413387892734531L
        }

        private constructor() : this("", 0, "")
    }

    fun createPerson(name: String = randomName, id: Int = randomInt, phone: String = randomPhone): Person {
        return Person(name, id, phone)
    }
}
