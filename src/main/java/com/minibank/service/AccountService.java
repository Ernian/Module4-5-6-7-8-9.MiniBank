package com.minibank.service;

import com.minibank.dto.AccountDto;
import com.minibank.entity.Account;
import com.minibank.entity.Client;
import com.minibank.repository.AccountRepository;
import com.minibank.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    public AccountService(
            AccountRepository accountRepository,
            ClientRepository clientRepository
    ) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
    }

    public AccountDto findById(long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Account with id %d not found", id)
                ));

        return AccountDto.fromEntity(account);
    }

    public AccountDto openAccount(AccountDto accountDto) {
        Client client = clientRepository.findById(accountDto.clientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Client with id %d not found", accountDto.clientId())
                ));

        Account accountToSave = new Account(
                client,
                accountDto.accountNumber(),
                BigDecimal.ZERO
        );

        Account savedAccount = accountRepository.save(accountToSave);
        return AccountDto.fromEntity(savedAccount);
    }
}
