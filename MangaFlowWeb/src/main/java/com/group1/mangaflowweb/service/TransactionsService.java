package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.subscription.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.transaction.TransactionsDTO;
import com.group1.mangaflowweb.dto.transaction.TransactionAdminDTO;
import com.group1.mangaflowweb.dto.transaction.TransactionSummaryDTO;
import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.enums.TransactionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionsService {

    // --- CLIENTS ---
    TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price);
    TransactionsDTO completeTransaction(Integer transactionId);
    TransactionsDTO completeTransaction(Integer transactionId, Long discountAmount, boolean isUpgrade);
    TransactionsDTO createAndCompleteTransaction(Integer userId, Integer subscriptionId, BigDecimal price);
    TransactionsDTO failTransaction(Integer transactionId);
    String getMembershipFromPrice(BigDecimal price);
    Long getCurrentMembershipPrice(Integer userId);
    SubscriptionCheckDTO checkSubscription(Integer userId, Long newSubscriptionPrice, BigDecimal newSubscriptionPriceBigDecimal);
    List<Transactions> getTransactionsByUserId(Integer userId);

    // --- ADMIN ---
    BigDecimal getTotalRevenue();
    List<Map<String, Object>> getRevenueBySubscription();
    List<Transactions> getAllTransactions();
    List<Transactions> getActiveTransactions();
    long getTotalTransactionCount();
    Page<TransactionAdminDTO> getTransactionsPage(Pageable pageable, TransactionEnum statusFilter, String usernameFilter);
    TransactionSummaryDTO getTransactionSummary();
    void cancelExpiredTransactionsAndDowngradeUsers();
}
