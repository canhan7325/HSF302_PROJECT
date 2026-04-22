package com.group1.mangaflowweb.util;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Utility class để tạo admin user với password đã hash
 * Chạy hàm main này một lần để tạo admin user
 * Sau đó xóa class này hoặc comment lại hàm main
 */
public class CreateAdminUser {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(
            new Class[]{com.group1.mangaflowweb.MangaFlowWebApplication.class},
            args
        );
        
        UserRepository userRepository = context.getBean(UserRepository.class);
        
        try {
            // Check if admin already exists
            if (userRepository.findByUsername("admin1").isPresent()) {
                System.out.println("✗ Admin user already exists!");
                context.close();
                System.exit(0);
            }
            
            // Hash password
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode("admin123");
            
            // Create admin user
            Users adminUser = Users.builder()
                    .username("admin1")
                    .email("admin@mangacurator.com")
                    .password(hashedPassword) // ✅ Hash password
                    .role("ADMIN") // ✅ Set role to ADMIN (uppercase)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            userRepository.save(adminUser);
            
            System.out.println("✓ Admin user created successfully!");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123");
            System.out.println("  Role: ADMIN");
            System.out.println("\n⚠️  Please delete this file or comment main() after creating admin user!");
            
        } catch (Exception e) {
            System.out.println("✗ Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
            System.exit(0);
        }
    }
}


