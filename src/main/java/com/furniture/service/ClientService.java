package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.dto.CreateClientRequest;
import com.furniture.entity.Client;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.ClientRepository;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client createClient(CreateClientRequest request) {
    String email = request.getEmail();
    if (email != null) {
        email = email.trim().toLowerCase();
        if (email.isEmpty()) {
            email = null;
        }
    }

    if (email != null && clientRepository.existsByEmailIgnoreCase(email)) {
        throw new DuplicateResourceException("Email already exists");
    }

    Client client = new Client();
    client.setName(request.getName());
    client.setPhone(request.getPhone());
    client.setEmail(email);
    client.setAddress(request.getAddress());
    return clientRepository.save(client);
}
    public List<Client> getAllClient() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found with id " + id));
    }
}
