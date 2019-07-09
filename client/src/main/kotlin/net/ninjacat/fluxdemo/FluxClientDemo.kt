package net.ninjacat.fluxdemo

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient


@SpringBootApplication
class FluxClientDemo {
    internal val logger = LoggerFactory.getLogger(FluxClientDemo::class.java)

    @Autowired
    private lateinit var context: ConfigurableApplicationContext

    @Bean
    internal fun getWebClient(): WebClient {
        return WebClient.create("http://localhost:8080")
    }

    @Bean
    internal fun demo(client: WebClient): CommandLineRunner {
        return CommandLineRunner {
            client.get()
                    .uri("/data")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .retrieve()
                    .bodyToFlux(DataObject::class.java)
                    .subscribe(
                            { msg ->
                                logger.info("Message: {}", msg)
                                Thread.sleep(25)
                            },
                            { err -> logger.error("Error", err) },
                            {
                                logger.info("Completed")
                                context.close()
                            })
        }
    }

}

fun main(args: Array<String>) {
    SpringApplicationBuilder(FluxClientDemo::class.java)
            .properties(java.util.Collections.singletonMap<String, Any>("server.port", "8081"))
            .run(*args)
}
