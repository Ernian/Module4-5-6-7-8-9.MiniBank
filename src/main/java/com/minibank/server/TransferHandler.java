package com.minibank.server;

import com.minibank.server.dto.TransferRequest;
import com.minibank.server.dto.TransferResponse;
import com.minibank.service.BankService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.List;

public class TransferHandler implements HttpHandler {
    private final BankService bankService;
    private final TransferRequestValidator validator;

    public TransferHandler(BankService bankService, TransferRequestValidator validator) {
        this.bankService = bankService;
        this.validator = validator;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = RequestBodyParser.getRequestMethod(exchange);
            String body = RequestBodyParser.getRequestBody(exchange);
            List<String> errors = validator.validate(method, body);

            if (!errors.isEmpty()) {
                ResponseSender.sendJson(exchange, 400, errors);
                return;
            }

            TransferRequest transfer = RequestBodyParser.mapToObject(body, TransferRequest.class);

            boolean successTransfer = bankService.transfer(
                    transfer.fromAccountId(),
                    transfer.toAccountId(),
                    transfer.amount()
            );

            ResponseSender.sendJson(
                    exchange,
                    201,
                    new TransferResponse(
                            successTransfer,
                            transfer.fromAccountId(),
                            transfer.toAccountId(),
                            transfer.amount()
                    )
            );

        } catch (Exception e) {
            System.out.println(e.getMessage() + '\n' + e);
            ResponseSender.sendJson(exchange, 500, "INTERNAL_ERROR");
        } finally {
            exchange.close();
        }
    }
}
