package com.minibank.server;

import com.sun.net.httpserver.HttpExchange;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RequestBodyParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String getRequestMethod(HttpExchange exchange) {
        return exchange.getRequestMethod();
    }

    public static String getRequestBody(HttpExchange exchange) {
        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return body;
    }

    public static <T> T mapToObject(String body, Class<T> type) {
        return MAPPER.readValue(body, type);
    }

    public static byte[] mapToBytes(Object payload) {
        return MAPPER.writeValueAsBytes(payload);
    }
}
