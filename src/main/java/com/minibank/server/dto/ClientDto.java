package com.minibank.server.dto;

import com.minibank.entity.Account;
import com.minibank.entity.Client;

import java.util.ArrayList;
import java.util.List;

public record ClientDto(
        Long id,
        String name,
        String email,
        List<Long> accountIds
) {
    public static ClientDto fromEntity(Client client) {
        List<Long> accountsIds = new ArrayList<>();

        if (client.getAccounts() != null) {
            accountsIds = client.getAccounts().stream()
                    .map(Account::getId)
                    .toList();
        }

        return new ClientDto(
                client.getId(),
                client.getName(),
                client.getEmail(),
                accountsIds
        );
    }
}
