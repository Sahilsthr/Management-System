package com.furniture.service;

import com.furniture.entity.Client;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.furniture.repository.ClientRepository;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAllClient() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }
}
