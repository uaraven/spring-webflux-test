package net.ninjacat.fluxdemo.akka;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshalling.sse.EventStreamMarshalling;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class HttpServer extends AllDirectives {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(final String[] args) throws Exception {
        // boot up server using the route as defined below
        final ActorSystem system = ActorSystem.create("routes");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
        final HttpServer app = new HttpServer();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    private Stream<DataObject> generateDataStream() {
        return Stream.generate(DataObject.Companion::make).limit(3000);
    }

    private String convertData(final DataObject data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<ServerSentEvent> eventStream() {
        return generateDataStream().map(data -> ServerSentEvent.create(convertData(data)));
    }

    private Iterable<ServerSentEvent> createEvents() {
        return () -> eventStream().iterator();
    }

    private Route createRoute() {
        return concat(
                path("data", () ->
                        get(() -> completeOK(Source.from(createEvents()), EventStreamMarshalling.toEventStream()))));
    }
}