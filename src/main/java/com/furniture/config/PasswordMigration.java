package com.furniture.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.furniture.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.furniture.entity.User;



@Component
public class PasswordMigration implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigration(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        for (User user : userRepository.findAll()) {
            String password = user.getPassword();
            if(password != null && !password.startsWith("$2")){
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            }
        }
        
    }
    
}
