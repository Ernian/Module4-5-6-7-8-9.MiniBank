package com.minibank.server;

import com.minibank.entity.Account;
import com.minibank.server.dto.AccountDto;
import com.minibank.service.BankService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AccountHandler implements HttpHandler {
    private final BankService bankService;

    public AccountHandler(BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String body = RequestBodyParser.getRequestBody(exchange);
            AccountDto accountDtoDto = RequestBodyParser.mapToObject(body, AccountDto.class);

            Account account = bankService.openAccount(accountDtoDto.clientId(), accountDtoDto.accountNumber());

            ResponseSender.sendJson(
                    exchange,
                    201,
                    AccountDto.fromEntity(account)
            );

        } catch (Exception e) {
            System.out.println(e.getMessage() + '\n' + e);
            ResponseSender.sendJson(exchange, 500, "INTERNAL_ERROR");
        } finally {
            exchange.close();
        }
    }
}
