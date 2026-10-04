package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
    
}
