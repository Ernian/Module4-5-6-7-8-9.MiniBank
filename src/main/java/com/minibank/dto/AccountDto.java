package com.minibank.dto;

import com.minibank.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountDto(
        Long id,

        @NotNull
        @Positive
        Long clientId,

        @NotBlank
        @Size(max = 34)
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
