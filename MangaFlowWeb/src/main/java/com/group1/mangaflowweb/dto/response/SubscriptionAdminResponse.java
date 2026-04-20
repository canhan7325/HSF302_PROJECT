package com.group1.mangaflowweb.dto.response;

import java.math.BigDecimal;

public record SubscriptionAdminResponse(
        Integer subscriptionId,
        String name,
        String description,
        BigDecimal price,
        Integer durationDays,
        Boolean isActive,
        long subscriberCount
) {}
