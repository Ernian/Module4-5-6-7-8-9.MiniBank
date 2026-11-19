package com.minibank.server;

import com.minibank.service.AccountService;
import com.minibank.service.BankService;
import com.minibank.service.ClientService;
import com.minibank.service.TransactionService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MiniBankServer {
    private static final int PORT = 8080;
    private static final String PATH = "/transfer";


    public static void main(String[] args) throws IOException {
        ClientService clientService = new ClientService();
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService();
        BankService bankService = new BankService(
                clientService,
                accountService,
                transactionService
        );
        TransferRequestValidator validator = new TransferRequestValidator();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(PATH, new TransferHandler(bankService,validator));

        server.start();
    }
}
