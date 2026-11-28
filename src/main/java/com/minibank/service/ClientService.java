package com.minibank.service;

import com.minibank.dto.ClientDto;
import com.minibank.entity.Client;
import com.minibank.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ClientDto findById(long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Client with id %d not found", id)
                ));

        return ClientDto.fromEntity(client);
    }

    public ClientDto registerClient(ClientDto clientDto) {
        Client savedClient = clientRepository.save(
                ClientDto.toEntity(clientDto)
        );

        return ClientDto.fromEntity(savedClient);
    }
}
