package com.group1.mangaflowweb.util;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Utility class để tạo tài khoản ADMIN
 * Chạy hàm main này một lần để tạo admin user
 * Sau đó xóa file này hoặc comment lại hàm main
 * 
 * Username: admin
 * Password: hahaha123
 * Role: ADMIN
 */
public class AdminUserCreator {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(
            new Class[]{com.group1.mangaflowweb.MangaFlowWebApplication.class}, 
            args
        );
        UserRepository userRepository = context.getBean(UserRepository.class);
        
        try {
            // Cấu hình mã hóa mật khẩu
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            
            // Kiểm tra xem admin user đã tồn tại chưa
            Optional<Users> existingAdmin = userRepository.findByUsername("admin1");
            
            if (existingAdmin.isPresent()) {
                System.out.println("✗ Admin user đã tồn tại!");
                System.exit(0);
            }
            
            // Tạo admin user mới
            Users adminUser = Users.builder()
                    .username("admin")
                    .email("admin@mangacurator.com")
                    .password(passwordEncoder.encode("hahaha123"))  // Mã hóa mật khẩu
                    .role("ADMIN")
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // Lưu vào database
            Users savedUser = userRepository.save(adminUser);
            
            System.out.println("✓ Tài khoản ADMIN đã được tạo thành công!");
            System.out.println("  Username: " + savedUser.getUsername());
            System.out.println("  Email: " + savedUser.getEmail());
            System.out.println("  Role: " + savedUser.getRole());
            System.out.println("  User ID: " + savedUser.getUserId());
            System.out.println("\n⚠️  Vui lòng xóa file này hoặc comment lại hàm main sau khi tạo xong!");
            
        } catch (Exception e) {
            System.out.println("✗ Lỗi khi tạo admin user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng Spring context
            context.close();
            System.exit(0);
        }
    }
}

