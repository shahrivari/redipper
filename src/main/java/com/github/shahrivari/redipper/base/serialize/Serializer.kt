package com.github.shahrivari.redipper.base.serialize

import io.objects.tl.api.TLApiContext
import io.objects.tl.core.TLObject

interface Serializer<V> {
    fun serialize(value: V): ByteArray
    fun deserialize(bytes: ByteArray): V?
}

class IntSerializer : Serializer<Int> {
    override fun serialize(value: Int) = value.toString().toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes).toInt()
}

class TLObjectSerializer<T : TLObject> : Serializer<T> {
    override fun serialize(value: T): ByteArray = value.serialize()

    override fun deserialize(bytes: ByteArray) =
            TLApiContext.getInstance().deserializeMessage(bytes.copyOfRange(0, bytes.size)) as T
}

class StringSerializer : Serializer<String> {
    override fun serialize(value: String) = value.toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes)
}

class LongSerializer : Serializer<Long> {
    override fun serialize(value: Long) = value.toString().toByteArray()

    override fun deserialize(bytes: ByteArray) = String(bytes).toLong()
}