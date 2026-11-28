package com.minibank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        @Positive
        Long fromAccountId,

        @NotNull
        @Positive
        Long toAccountId,

        @NotNull
        @Positive
        BigDecimal amount
) {
}
