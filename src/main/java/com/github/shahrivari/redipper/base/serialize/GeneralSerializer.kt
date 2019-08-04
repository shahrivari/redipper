package com.github.shahrivari.redipper.base.serialize

import io.objects.tl.api.TLApiContext
import io.objects.tl.core.TLObject
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.reflect.full.isSubclassOf

class GeneralSerializer<V : Any>(private val clazz: Class<V>) : Serializer<V> {

    private val kryo = KryoSerializer<V>()

    init {
        require(hasDefaultConstructorAndSerialVersionUID(clazz))
    }

    private fun hasDefaultConstructorAndSerialVersionUID(clazz: Class<*>): Boolean {
        if (clazz.isPrimitive || clazz.kotlin.isSubclassOf(TLObject::class)) {
            return true
        }

        try {
            clazz.getDeclaredField("serialVersionUID")
        } catch (e: NoSuchFieldException) {
            logger.error { "class $clazz does not have a serialVersionUID and thus cannot be cached!" }
            return false
        }

        try {
            clazz.getDeclaredConstructor()
        } catch (e: NoSuchFieldException) {
            logger.error { "class $clazz does not have a default constructor!" }
            return false
        }

        return true
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val TL_SERIALIZE_HEADER: Byte = 1
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

        if (value is TLObject) {
            return value.serialize().prepend(TL_SERIALIZE_HEADER)
        }

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

    override fun deserialize(bytes: ByteArray): V? {
        require(bytes.isNotEmpty()) { "content must not be null." }

        return try {
            val objectStream = ByteArrayInputStream(bytes, 1, bytes.size - 1)
            when (bytes.first()) {
                TL_SERIALIZE_HEADER -> TLApiContext.getInstance().deserializeMessage(objectStream) as V

                KRYO_SERIALIZE_HEADER -> kryo.deserialize(objectStream)

                JAVA_SERIALIZE_HEADER -> ObjectInputStream(objectStream).readObject() as V

                else -> error("Serialization HEADER not supported : ${bytes.first()}")
            }
        } catch (t: Throwable) {
            logger.error(t) { "Cannot deserialize object of class <$clazz>: ${t.message}" }
            null
        }
    }
}