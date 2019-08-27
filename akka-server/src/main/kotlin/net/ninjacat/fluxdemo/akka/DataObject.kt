package net.ninjacat.fluxdemo.akka

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import kotlin.math.abs

data class DataObject(val id: Int, val name: String, val data: String) {
    companion object {
        private val counter = AtomicInteger()
        private val random = Random()

        private fun randomLetter(): String {
            return (random.nextInt('z' - 'a') + 'a'.toInt()).toChar().toString()
        }

        private fun randomString(len: Int): String {
            return (0 until len).joinToString("", transform = { randomLetter() })
        }

        private fun makeName(): String {
            return randomString(5) + (0 until 5).joinToString(separator = "", transform = { random.nextInt(10).toString() }) + randomString(3)
        }

        private fun makeRandomData(): String {
            return random.ints(100).mapToObj { abs(it).toString(16) }.collect(Collectors.joining())
        }

        fun make(): DataObject {
            return DataObject(counter.incrementAndGet(), makeName(), makeRandomData())
        }
    }
}