package com.github.shahrivari.redipper.base.serialize

interface Serializer<V> {
    fun serialize(value: V): ByteArray
    fun deserialize(bytes: ByteArray): V?
}

class IntSerializer : Serializer<Int> {
    override fun serialize(value: Int) = value.toString().toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes).toInt()
}

class StringSerializer : Serializer<String> {
    override fun serialize(value: String) = value.toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes)
}

class LongSerializer : Serializer<Long> {
    override fun serialize(value: Long) = value.toString().toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes).toLong()
}