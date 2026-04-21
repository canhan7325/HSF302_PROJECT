package com.group1.mangaflowweb.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionAdminResponse {
    private Integer subscriptionId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Boolean isActive;
    private long subscriberCount;
}
