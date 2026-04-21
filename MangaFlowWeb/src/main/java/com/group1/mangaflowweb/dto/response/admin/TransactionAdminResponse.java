package com.group1.mangaflowweb.dto.response.admin;

import com.group1.mangaflowweb.enums.TransactionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAdminResponse {
    private Integer transactionId;
    private String username;
    private String subscriptionName;
    private BigDecimal price;
    private TransactionEnum status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
}
