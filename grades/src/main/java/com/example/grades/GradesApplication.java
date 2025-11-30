package com.example.grades;

import com.example.grades.model.User;
import com.example.grades.repo.UserRepository;
import com.example.grades.security.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class GradesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradesApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }
        };
    }
}
