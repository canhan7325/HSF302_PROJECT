package com.group1.mangaflowweb.config;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfNotExists(userRepository, passwordEncoder,
                    "author1", "author1@gmail.com");
            createUserIfNotExists(userRepository, passwordEncoder,
                    "author2", "author2@gmail.com");

            createUserIfNotExists(userRepository, passwordEncoder,
                    "author3", "author3@gmail.com");

            createUserIfNotExists(userRepository, passwordEncoder,
                    "author4", "author4@gmail.com");

            createUserIfNotExists(userRepository, passwordEncoder,
                    "author5", "author5@gmail.com");

            createUserIfNotExists(userRepository, passwordEncoder,
                    "author6", "author6@gmail.com");
        };
    }

    private void createUserIfNotExists(UserRepository repo,
                                       PasswordEncoder encoder,

                                       String username,
                                       String email) {

        if (!repo.existsByUsername(username)) {
            Users user = new Users();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(encoder.encode("1")); // password = 1
            user.setRole("AUTHOR");
            user.setEnabled(true);

            repo.save(user);
        }
    }
}
