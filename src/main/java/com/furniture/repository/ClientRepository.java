package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long>{
    
    
}
