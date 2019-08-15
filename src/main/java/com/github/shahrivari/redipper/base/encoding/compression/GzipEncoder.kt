package com.github.shahrivari.redipper.base.encoding.compression

import com.github.shahrivari.redipper.base.encoding.Encoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class GzipEncoder : Encoder {
    override fun encode(bytes: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream(bytes.size)
        GZIPOutputStream(baos).use {
            it.write(bytes)
        }
        return baos.toByteArray()
    }

    override fun decode(bytes: ByteArray): ByteArray =
            GZIPInputStream(ByteArrayInputStream(bytes)).readBytes()
}