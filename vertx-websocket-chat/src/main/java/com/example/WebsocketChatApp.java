package com.example;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebsocketChatApp {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);

        // handle top page.
        router.route("/").handler(r -> r.response().sendFile("chat.html"));

        router.route("/myapp/*").handler(buildSockJSHandler(vertx));

        // handle static contents
        router.route("/static/*").handler(StaticHandler.create());

        httpServer.requestHandler(router::accept).listen(8181);
    }

    private static SockJSHandler buildSockJSHandler(Vertx vertx) {
        ConcurrentHashMap<String, SockJSSocket> sockets = new ConcurrentHashMap<>();

        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        sockJSHandler.socketHandler(sockJSSocket -> {
            log.info("websocket established: {}", sockJSSocket.writeHandlerID());

            // register new socket.
            sockets.put(sockJSSocket.writeHandlerID(), sockJSSocket);

            // Just echo the data back
            sockJSSocket.handler(buffer -> {
                log.info("Sending message '{}' to {} recipients.", buffer.toString(),
                        sockets.size());

                // send to clients
                sockets.values().forEach(sock -> {
                    sock.write(buffer);
                });
            });
            // closed event
            sockJSSocket.endHandler(event -> {
                log.info("end: {}", sockJSSocket.writeHandlerID());
                sockets.remove(sockJSSocket.writeHandlerID());
            });
        });
        return sockJSHandler;
    }
}
