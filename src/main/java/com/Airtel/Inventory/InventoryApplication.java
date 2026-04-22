package com.Airtel.Inventory;

import com.Airtel.Inventory.model.User;
import com.Airtel.Inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class InventoryApplication implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        System.out.println("=========================================");
        System.out.println("Inventory Management System Started!");
        System.out.println("Access: http://localhost:8080");
        System.out.println("=========================================");
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("josue")) {
            User admin = new User();
            admin.setUsername("josue");
            admin.setPassword(passwordEncoder.encode("josue123"));
            admin.setRole("ADMIN");
            admin.setFullName("Josue Administrator");
            admin.setEmail("josue@airtel.com");
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("Admin user created - Username: josue, Password: josue123");
        }
    }
}