package com.github.shahrivari.redipper.base.serialize

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class KryoSerializer<V> {

    private val kryos = object : ThreadLocal<Kryo>() {
        override fun initialValue(): Kryo {
            return Kryo().apply { isRegistrationRequired = false }
        }
    }

    private fun kryoSerialize(value: V): ByteArray {
        val stream = ByteArrayOutputStream()
        val output = Output(stream, 128)
        kryos.get().writeClassAndObject(output, value)
        output.close()
        return stream.toByteArray()
    }

    fun serialize(value: V): ByteArray {
        return kryoSerialize(value)
    }

    fun deserialize(objectStream: ByteArrayInputStream): V {
        val input = Input(objectStream)
        return kryos.get().readClassAndObject(input) as V
    }
}