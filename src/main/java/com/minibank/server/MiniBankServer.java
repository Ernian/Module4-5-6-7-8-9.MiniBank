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
    private static final String CLIENT_PATH = "/client";
    private static final String ACCOUNT_PATH = "/account";
    private static final String TRANSFER_PATH = "/transfer";


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
        server.createContext(CLIENT_PATH, new ClientHandler(bankService));
        server.createContext(ACCOUNT_PATH, new AccountHandler(bankService));
        server.createContext(TRANSFER_PATH, new TransferHandler(bankService, validator));

        server.start();
    }
}
