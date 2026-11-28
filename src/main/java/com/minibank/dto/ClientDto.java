package com.minibank.dto;

import com.minibank.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientDto(
        Long id,

        @NotBlank
        @Size(min = 2, max = 255)
        String name,

        @Email
        @NotBlank
        @Size(min = 2, max = 255)
        String email
) {
    public static Client toEntity(ClientDto clientDto) {
        return new Client(
                clientDto.name(),
                clientDto.email()
        );
    }

    public static ClientDto fromEntity(Client client) {
        return new ClientDto(
                client.getId(),
                client.getName(),
                client.getEmail()
        );
    }
}
