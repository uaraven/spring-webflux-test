package net.ninjacat.fluxdemo

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.stream.Stream

@RestController
class ServerController {

    internal var logger = LoggerFactory.getLogger(ServerController::class.java!!)

    @GetMapping(value = "/data", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getData(): Flux<DataObject> {
        return Flux.fromStream(Stream.generate { DataObject.make() }
                .limit(10)
                .peek { msg -> logger.info(msg.toString()) })
    }

}