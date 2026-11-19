package com.minibank.server;

import com.minibank.server.dto.TransferRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransferRequestValidator {

    public List<String> validate(String method, String body) {
        List<String> errors = new ArrayList<>();
        validateHttpMethod(method, errors);

        if (body.isBlank()) {
            errors.add("Request body is empty");
            return errors;
        }

        TransferRequest transfer = RequestBodyParser.mapToObject(body, TransferRequest.class);
        validateJson(transfer, errors);
        checkDifferentAccountIds(transfer, errors);

        return errors;
    }

    public void validateHttpMethod(String method, List<String> errors) {
        if (!"POST".equalsIgnoreCase(method)) {
            errors.add("Method not allowed, only POST is supported");
        }
    }

    private void validateJson(TransferRequest transfer, List<String> errors) {
        if (transfer.fromAccountId() == null || transfer.fromAccountId() < 0) {
            errors.add("Invalid sender account id");
        }

        if (transfer.toAccountId() == null || transfer.toAccountId() < 0) {
            errors.add("Invalid recipient account id");
        }

        if (transfer.amount() == null || transfer.amount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Invalid amount of transfer");
        }
    }

    private void checkDifferentAccountIds(TransferRequest transfer, List<String> errors) {
        Long fromId = transfer.fromAccountId();
        Long toId = transfer.toAccountId();

        if (fromId != null && fromId.equals(toId)) {
            errors.add("The sender’s and recipient’s accounts are the same");
        }
    }
}
