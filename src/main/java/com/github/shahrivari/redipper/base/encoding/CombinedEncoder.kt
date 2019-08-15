package com.github.shahrivari.redipper.base.encoding

class CombinedEncoder(private val encoders: Array<out Encoder>) : Encoder {

    companion object {
        fun of(vararg encoders: Encoder): Encoder =
                CombinedEncoder(encoders)
    }

    override fun encode(bytes: ByteArray): ByteArray {
        var input = bytes
        encoders.forEach {
            input = it.encode(input)
        }
        return input
    }

    override fun decode(bytes: ByteArray): ByteArray {
        var input = bytes
        encoders.reversed().forEach {
            input = it.decode(input)
        }
        return input
    }
}