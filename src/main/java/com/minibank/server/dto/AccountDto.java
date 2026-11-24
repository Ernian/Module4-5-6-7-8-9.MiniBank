package com.minibank.server.dto;

import com.minibank.entity.Account;

import java.math.BigDecimal;

public record AccountDto(
        Long id,
        Long clientId,
        String accountNumber,
        BigDecimal balance
) {
    public static AccountDto fromEntity(Account account) {
        return new AccountDto(
                account.getId(),
                account.getClient().getId(),
                account.getAccountNumber(),
                account.getBalance()
        );
    }
}
