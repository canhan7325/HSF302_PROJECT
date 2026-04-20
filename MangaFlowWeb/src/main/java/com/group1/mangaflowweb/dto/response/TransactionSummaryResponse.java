package com.group1.mangaflowweb.dto.response;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        long totalCount,
        BigDecimal totalRevenue
) {}
