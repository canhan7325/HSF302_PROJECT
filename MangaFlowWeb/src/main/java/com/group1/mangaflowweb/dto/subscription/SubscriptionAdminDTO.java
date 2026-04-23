package com.group1.mangaflowweb.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionAdminDTO {
    private Integer subscriptionId;

    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal price;

    @Positive
    private Integer durationDays;

    private Boolean isActive;
    private long subscriberCount;
}
