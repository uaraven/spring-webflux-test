package net.ninjacat.fluxdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FluxDemoApplication

fun main(args: Array<String>) {
	runApplication<FluxDemoApplication>(*args)
}
