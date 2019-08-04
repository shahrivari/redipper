package com.github.shahrivari.redipper.util

import java.security.SecureRandom
import java.util.*
import kotlin.streams.asSequence

private val secureRandom = object : SecureRandom() {
    override fun nextInt(bound: Int): Int {
        return super.nextInt(bound) + 1
    }
}

interface BaseTest {
    private val source get() = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random get() = secureRandom
    val randomLong get() = Math.abs(random.nextLong()) + 1
    val randomInt get() = random.nextInt()
    val randomNatural get() = Math.abs(random.nextInt()) + 1
    val randomName get() = "Test$randomNatural"

    fun randomString(len: Int) =
            Random().ints(len.toLong(), 0, source.length).asSequence().map(
                    source::get).joinToString("")

    fun getRandomInt(start: Int, endInclusive: Int): Int =
            random.nextInt(endInclusive + 1 - start) + start

    fun randomIp() =
            "${random.nextInt(256)}." +
                    "${random.nextInt(256)}." +
                    "${random.nextInt(256)}." +
                    "${random.nextInt(256)}"

    fun getRandomBytes(size: Int): ByteArray {
        val result = ByteArray(size)
        random.nextBytes(result)
        return result
    }
}