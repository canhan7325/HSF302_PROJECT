package com.group1.mangaflowweb.dto.transaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionsDTO {
    private Integer transactionId;
    private BigDecimal price;
    private String status;
    private String statusSubs;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private Integer userId;
    private Integer subscriptionId;
    private String subscriptionName;
}
