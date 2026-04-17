package com.group1.mangaflowweb.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionsDTO {
    private Integer subscriptionId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Boolean isActive;
}
