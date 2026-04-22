package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.TransactionsDTO;
import com.group1.mangaflowweb.dto.response.admin.TransactionAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.TransactionSummaryResponse;
import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    // ── Payment flow ──────────────────────────────────────────────────────────
    TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price);
    TransactionsDTO completeTransaction(Integer transactionId);
    TransactionsDTO completeTransaction(Integer transactionId, Long discountAmount, boolean isUpgrade);
    TransactionsDTO createAndCompleteTransaction(Integer userId, Integer subscriptionId, BigDecimal price);

    // ── Subscription checks ───────────────────────────────────────────────────
    SubscriptionCheckDTO checkSubscription(Integer userId, Long newSubscriptionPrice, BigDecimal newSubscriptionPriceBigDecimal);
    String getMembershipFromPrice(BigDecimal price);
    Long getCurrentMembershipPrice(Integer userId);
    List<Transactions> getTransactionsByUserId(Integer userId);

    // ── Admin ─────────────────────────────────────────────────────────────────
    BigDecimal getTotalRevenue();
    List<Map<String, Object>> getRevenueBySubscription();
    List<Transactions> getAllTransactions();
    List<Transactions> getActiveTransactions();
    long getTotalTransactionCount();
    Page<TransactionAdminResponse> getTransactionsPage(Pageable pageable, ComicEnum statusFilter, String usernameFilter);
    TransactionSummaryResponse getTransactionSummary();
    void cancelExpiredTransactionsAndDowngradeUsers();
}
