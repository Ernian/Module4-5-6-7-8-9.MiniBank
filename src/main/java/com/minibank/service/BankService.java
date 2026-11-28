package com.minibank.service;

import com.minibank.dto.TransferRequest;
import com.minibank.dto.TransferResponse;
import com.minibank.entity.Account;
import com.minibank.entity.Transaction;
import com.minibank.entity.TransactionType;
import com.minibank.repository.AccountRepository;
import com.minibank.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        Account fromAccount = getAccountById(request.fromAccountId());
        Account toAccount = getAccountById(request.toAccountId());

        BigDecimal fromBalance = fromAccount.getBalance();
        BigDecimal toBalance = toAccount.getBalance();
        BigDecimal amount = request.amount();

        fromAccount.setBalance(fromBalance.subtract(amount));
        toAccount.setBalance(toBalance.add(amount));

        Transaction transactionOut = new Transaction(
                fromAccount,
                amount.negate(),
                TransactionType.TRANSFER_OUT
        );

        Transaction transactionIn = new Transaction(
                toAccount,
                amount,
                TransactionType.TRANSFER_IN
        );

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionRepository.save(transactionOut);
        transactionRepository.save(transactionIn);

        return new TransferResponse(
                true,
                request.fromAccountId(),
                request.toAccountId(),
                amount,
                LocalDateTime.now().toString()
        );
    }

    private Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Account with id %d not found", id)
                ));
    }
}
