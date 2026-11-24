package com.minibank.server;

import com.minibank.entity.Client;
import com.minibank.server.dto.ClientDto;
import com.minibank.service.BankService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClientHandler implements HttpHandler {
    private final BankService bankService;

    public ClientHandler(BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = RequestBodyParser.getRequestMethod(exchange);

            if ("GET".equalsIgnoreCase(method)) {
                ResponseSender.sendJson(exchange, 200, bankService.getAllClientsDto());
                return;
            }

            String body = RequestBodyParser.getRequestBody(exchange);
            ClientDto clientDto = RequestBodyParser.mapToObject(body, ClientDto.class);

            Client client = bankService.registerClient(clientDto.name(), clientDto.email());

            ResponseSender.sendJson(
                    exchange,
                    201,
                    ClientDto.fromEntity(client)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage() + '\n' + e);
            ResponseSender.sendJson(exchange, 500, "INTERNAL_ERROR");
        } finally {
            exchange.close();
        }
    }
}
