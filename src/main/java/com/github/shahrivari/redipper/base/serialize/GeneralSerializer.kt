package com.github.shahrivari.redipper.base.serialize

import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class GeneralSerializer<V : Any>(private val clazz: Class<V>) : Serializer<V> {

    private val kryo = KryoSerializer<V>()

    init {
        require(hasDefaultConstructor(clazz)) { "Class does not have default constructor: $clazz" }
        require(hasSerialVersionUID(clazz)) { "Class does not have SerialVersionUID: $clazz" }
    }

    private fun hasDefaultConstructor(clazz: Class<*>): Boolean {
        if (clazz.isPrimitive) {
            return true
        }

        try {
            clazz.getDeclaredConstructor()
        } catch (e: ReflectiveOperationException) {
            logger.error { "class $clazz does not have a default constructor!" }
            return false
        }

        return true
    }

    private fun hasSerialVersionUID(clazz: Class<*>): Boolean {
        if (clazz.isPrimitive) {
            return true
        }

        try {
            clazz.getDeclaredField("serialVersionUID")
        } catch (e: NoSuchFieldException) {
            logger.error { "class $clazz does not have a serialVersionUID and thus cannot be cached!" }
            return false
        }

        return true
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val JAVA_SERIALIZE_HEADER: Byte = 2
        private const val KRYO_SERIALIZE_HEADER: Byte = 3

        // store types which can' serialize with kryo.
        private val nonKryoTypes = mutableSetOf<String>()

        private fun Any.javaSerialize(): ByteArray {
            val bytes = ByteArrayOutputStream()
            val os = ObjectOutputStream(bytes)
            os.use { it.writeObject(this) }
            return bytes.toByteArray()
        }

        private fun ByteArray.prepend(byte: Byte): ByteArray {
            val result = ByteArray(size + 1)
            result[0] = byte
            copyInto(result, 1)
            return result
        }
    }

    override fun serialize(value: V): ByteArray {
        val clazz = value::class.java

        if (nonKryoTypes.contains(clazz.canonicalName)) {
            return value.javaSerialize().prepend(JAVA_SERIALIZE_HEADER)
        }

        return try {
            kryo.serialize(value).prepend(KRYO_SERIALIZE_HEADER)
        } catch (t: Throwable) {
            nonKryoTypes.add(clazz.canonicalName)
            logger.warn(t) { "Class ${clazz.canonicalName} cannot be serialized with Kryo." }
            value.javaSerialize().prepend(JAVA_SERIALIZE_HEADER)
        }
    }

    override fun deserialize(bytes: ByteArray): V {
        require(bytes.isNotEmpty()) { "content must not be null." }

        return try {
            val objectStream = ByteArrayInputStream(bytes, 1, bytes.size - 1)
            when (bytes.first()) {
                KRYO_SERIALIZE_HEADER -> kryo.deserialize(objectStream)

                JAVA_SERIALIZE_HEADER -> ObjectInputStream(objectStream).readObject() as V

                else -> error("Serialization HEADER not supported : ${bytes.first()}")
            }
        } catch (t: Throwable) {
            logger.error(t) { "Cannot deserialize object of class <$clazz>: ${t.message}" }
            throw t
        }
    }
}