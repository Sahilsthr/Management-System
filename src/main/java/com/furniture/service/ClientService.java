package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.dto.CreateClientRequest;
import com.furniture.entity.Client;
import com.furniture.repository.ClientRepository;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client createClient(CreateClientRequest request) {
        Client client = new Client();
        client.setName(request.getName());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());
        return clientRepository.save(client);
    }

    public List<Client> getAllClient() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }
}
