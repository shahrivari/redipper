package com.github.shahrivari.redipper.base.encoding.compression

import com.github.shahrivari.redipper.base.encoding.Encoder
import net.jpountz.lz4.LZ4FrameInputStream
import net.jpountz.lz4.LZ4FrameOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class Lz4Encoder : Encoder {
    override fun encode(bytes: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream(bytes.size)
        LZ4FrameOutputStream(baos).use {
            it.write(bytes) }
        return baos.toByteArray()
    }

    override fun decode(bytes: ByteArray): ByteArray =
            LZ4FrameInputStream(ByteArrayInputStream(bytes)).readBytes()
}