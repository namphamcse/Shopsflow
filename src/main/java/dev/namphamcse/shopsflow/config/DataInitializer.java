package dev.namphamcse.shopsflow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.entity.enums.Role;
import dev.namphamcse.shopsflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@shopsflow.com").isEmpty()) {
            User admin = new User();
            admin.setName("Nam Pham Hoang");
            admin.setEmail("admin@shopsflow.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            
            System.out.println("--------------------------------------");
            System.out.println("DEFAULT ADMIN CREATED: admin@shopsflow.com / admin123");
            System.out.println("--------------------------------------");
        }
    }
}
