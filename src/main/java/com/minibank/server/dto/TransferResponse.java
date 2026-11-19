package com.minibank.server.dto;

import java.math.BigDecimal;

public record TransferResponse(
        boolean success,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount
) {
}
