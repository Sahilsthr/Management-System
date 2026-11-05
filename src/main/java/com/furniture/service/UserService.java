package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreateUserRequest;
import com.furniture.entity.Role;
import com.furniture.entity.User;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (request.getRole() != Role.EMPLOYEE) {
            throw new BadRequestException(
                    "Only EMPLOYEE accounts can be created through this API");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone() == null
                ? null : request.getPhone().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EMPLOYEE);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
