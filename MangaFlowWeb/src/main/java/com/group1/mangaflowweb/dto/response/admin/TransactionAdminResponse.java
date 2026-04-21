package com.group1.mangaflowweb.dto.response.admin;

import com.group1.mangaflowweb.enums.ComicEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionAdminResponse(
        Integer transactionId,
        String username,
        String subscriptionName,
        BigDecimal price,
        ComicEnum status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt
) {}
