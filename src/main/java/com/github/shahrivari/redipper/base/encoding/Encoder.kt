package com.github.shahrivari.redipper.base.encoding

interface Encoder {
    fun encode(bytes: ByteArray): ByteArray
    fun decode(bytes: ByteArray): ByteArray
}