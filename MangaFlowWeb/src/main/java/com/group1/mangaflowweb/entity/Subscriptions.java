package com.group1.mangaflowweb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscriptions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subscriptionId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "VARCHAR(MAX)")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer durationDays;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Relationships
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transactions> transactions = new ArrayList<>();
}

