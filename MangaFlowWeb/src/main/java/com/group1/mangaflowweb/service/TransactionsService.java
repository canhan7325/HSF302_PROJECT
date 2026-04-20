package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.TransactionsDTO;
import java.math.BigDecimal;

public interface TransactionsService {
    TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price);
    TransactionsDTO completeTransaction(Integer transactionId);
    TransactionsDTO completeTransaction(Integer transactionId, Long discountAmount, boolean isUpgrade);
    TransactionsDTO createAndCompleteTransaction(Integer userId, Integer subscriptionId, BigDecimal price);

    /**
     * Determine membership label from subscription price
     */
    String getMembershipFromPrice(BigDecimal price);

    /**
     * Get current membership price of user (0 if no membership)
     */
    Long getCurrentMembershipPrice(Integer userId);

    /**
     * Check if can subscribe và trả về chi tiết
     */
    SubscriptionCheckDTO checkSubscription(Integer userId, Long newSubscriptionPrice, BigDecimal newSubscriptionPriceBigDecimal);
}


