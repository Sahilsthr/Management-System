package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.entity.Client;
import com.furniture.service.ClientService;


@RestController
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/api/clients")
    public Client createClient(@RequestBody Client client) {
        return clientService.createClient(client);
    }

    @GetMapping("/api/clients")
    public List<Client> getAllClient() {
        return clientService.getAllClient();
    }

    @GetMapping("/api/clients/{id}")
    public Optional<Client> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }
}
