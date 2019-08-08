package com.github.shahrivari.redipper.util

import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
interface AppTestUtils : BaseTest {

    val dogImage: ByteArray
        get() = {
            val image = ImageIO.read(File("src/test/resources/dog.jpg"))
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "jpg", outputStream)
            outputStream.toByteArray()
        }()

    val testVideoLight: ByteArray
        get() {
            val file = File("src/test/resources/test_video_light.mp4")
            return Files.readAllBytes(file.toPath())
        }

    val randomPhone
        get() = "+989" + (0..9).map { random.nextInt(10).toString() }
                .reduce { acc, i -> acc + i }

}