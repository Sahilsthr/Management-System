package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);
    
}
