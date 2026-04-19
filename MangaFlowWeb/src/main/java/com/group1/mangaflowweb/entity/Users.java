package com.group1.mangaflowweb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(nullable = false, unique = true, length = 100, name = "username")
    private String username;
    
    @Column(nullable = false, unique = true, length = 255, name = "email")
    private String email;
    
    @Column(nullable = false, length = 255, name = "password")
    private String password;
    
    @Column(nullable = false, length = 50, name = "role")
    @Builder.Default
    private String role = "user";
    
    @Column(nullable = false, name = "enabled")
    @Builder.Default
    private Boolean enabled = true;
    
    @Column(nullable = false, updatable = false, name = "created_at")
    @Builder.Default
    @ColumnDefault("GETDATE()")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comics> comics = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transactions> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bookmarks> bookmarks = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReadingHistories> readingHistories = new ArrayList<>();
}
