package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import lombok.extern.slf4j.Slf4j;

// http://vertx.io/docs/vertx-web/java/
@Slf4j
public class ExampleApp {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                // dropwizard metrics
                new DropwizardMetricsOptions()
                        .setEnabled(true)
                        .addMonitoredHttpServerUri(new Match().setValue("/"))
                        .setJmxEnabled(true)
        ));
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        // request body parser.
        // content-length を 30MB 上限とする。
        router.route().handler(
                BodyHandler.create()
                        .setBodyLimit(30_000_000));

        // enable cookie handler
        router.route().handler(CookieHandler.create());

        // static file
        router.route("/static/*").handler(
                StaticHandler.create());

        // timeout 処理
        router.route("/foo/").handler(TimeoutHandler.create(5000));

        // 共通の前処理とか
        router.get("/foo/*").handler(routingContext -> {
            System.out.println(routingContext.request().uri());
            routingContext.next();
        });

        router.route("/foo/bar").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.end("hello\n");
        });

        // capture path parameters
        router.get("/blog/:user/:entryId").handler(routingContext -> {
            String user = routingContext.request().getParam("user");
            String entryId = routingContext.request().getParam("entryId");

            routingContext.response()
                    .end("user: " + user + " entryId: " + entryId);
        });

        // json response
        router.get("/world.json")
                .produces("application/json")
                .handler(r -> r.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(
                                Json.encodePrettily(new Entity("Yay")))
                );

        // json request
        // curl -v -d '{"message":"hoge"}' http://localhost:8181/yabai.json
        router.route("/yabai.json")
                .handler(r -> {
                    Entity entity = Json.decodeValue(r.getBodyAsString(), Entity.class);
                    r.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(
                                    Json.encodePrettily(new Entity("Hello, " + entity.getMessage())));
                });

        router.route("/proxy")
                .handler(r -> {
                    HttpClient httpClient = r.vertx().createHttpClient(
                            new HttpClientOptions()
                            .setDefaultHost("mixi.jp")
                            .setDefaultPort(80)
                    );
                    System.out.println("requesting...");
                    HttpClientRequest httpClientRequest = httpClient.get("/", resp -> {
                        System.out.println(resp.statusCode());
                        r.response()
                                .putHeader("content-type", "text/plain")
                                .end(resp.statusMessage());
                    });
                    httpClientRequest.setTimeout(3_000);
                    httpClientRequest.exceptionHandler(throwable -> {
                        log.info("Exception when trying to invoke server: {}", throwable.getMessage());
                    });
                    httpClientRequest.end();
                });

        router.route("/exception")
                .handler(r -> {
                    throw new RuntimeException("exception~~");
                });

        // exception handler
        router.route().failureHandler(r -> {
            Throwable failure = r.failure();
            log.info("Got error", failure);

            r.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .end("Oops");
        });
        // not found handler
        router.route().last().handler(r -> {
            r.response().setStatusCode(404)
                    .end("Not found!!!!");
        });

        // http://vertx.io/blog/vert-x-application-configuration/
        server.requestHandler(router::accept).listen(
                vertx.getOrCreateContext().config().getInteger("http.port", 8181), handler -> {
                    if (handler.succeeded()) {
                        System.out.println("http://localhost:8181");
                    } else {
                        System.out.println("FAILed to listen");
                    }
                });
    }

    public static class Entity {
        private final String message;

        @JsonCreator
        public Entity(@JsonProperty("message") String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
