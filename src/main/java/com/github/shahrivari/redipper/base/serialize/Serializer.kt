package com.github.shahrivari.redipper.base.serialize

interface Serializer<V> {
    fun serialize(value: V): ByteArray
    fun deserialize(bytes: ByteArray): V?
}

class IntSerializer : Serializer<Int> {
    override fun serialize(value: Int) = value.toString().toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes).toIntOrNull()
}

class StringSerializer : Serializer<String> {
    override fun serialize(value: String) = "S$value".toByteArray()

    override fun deserialize(bytes: ByteArray) =
            if(bytes.isEmpty())
                null
            else
                String(bytes).substring(1)
}

class LongSerializer : Serializer<Long> {
    override fun serialize(value: Long) = value.toString().toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes).toLongOrNull()
}

class ByteArraySerializer : Serializer<ByteArray> {
    override fun serialize(value: ByteArray) = value

    override fun deserialize(bytes: ByteArray) = bytes
}
