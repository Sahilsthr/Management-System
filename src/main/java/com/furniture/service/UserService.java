package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.dto.CreateUserRequest;
import com.furniture.entity.User;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public User createUser(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        boolean emailExists = userRepository.findAll()
                .stream()
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email));
        if (emailExists) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        return userRepository.save(user);
    }
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }
    
}
