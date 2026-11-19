package com.minibank.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseSender {

    public static void sendJson(HttpExchange exchange, int status, Object payload) {
        byte[] bytes = RequestBodyParser.mapToBytes(payload);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(status, bytes.length);
            os.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
