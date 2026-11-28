package com.minibank.controller;

import com.minibank.dto.TransferRequest;
import com.minibank.dto.TransferResponse;
import com.minibank.service.BankService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfer")
public class TransferController {
    private final BankService bankService;

    public TransferController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest transferRequest) {
        return bankService.transfer(transferRequest);
    }
}
