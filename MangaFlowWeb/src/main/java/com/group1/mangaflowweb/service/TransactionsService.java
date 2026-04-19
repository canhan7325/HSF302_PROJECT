package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.TransactionsDTO;
import java.math.BigDecimal;

public interface TransactionsService {
    TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price);
    TransactionsDTO completeTransaction(Integer transactionId);
}
