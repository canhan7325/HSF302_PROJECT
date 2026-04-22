package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.dto.response.admin.TransactionAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.TransactionSummaryResponse;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    
    // Get total revenue
    BigDecimal getTotalRevenue();
    
    // Get revenue by subscription
    List<Map<String, Object>> getRevenueBySubscription();
    
    // Get all transactions (sorted by createdAt DESC)
    List<Transactions> getAllTransactions();
    
    // Get active transactions (ongoing)
    List<Transactions> getActiveTransactions();
    
    // Get transaction count
    long getTotalTransactionCount();
    
    // Get recent transactions with filters
    List<Transactions> getRecentTransactions(Integer limit, String statusFilter, String subscriptionFilter);

    // ===============================
    Page<TransactionAdminResponse> getTransactionsPage(Pageable pageable, ComicEnum statusFilter, String usernameFilter);

    TransactionSummaryResponse getTransactionSummary();
}
