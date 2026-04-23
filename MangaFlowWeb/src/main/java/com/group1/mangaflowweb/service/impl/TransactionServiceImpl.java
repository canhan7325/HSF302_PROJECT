package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.repository.TransactionRepository;
import com.group1.mangaflowweb.service.TransactionService;
import org.springframework.stereotype.Service;

import com.group1.mangaflowweb.dto.response.admin.TransactionAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.TransactionSummaryResponse;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    public BigDecimal getTotalRevenue() {
        return transactionRepository.getTotalRevenue();
    }
    
    @Override
    public List<Map<String, Object>> getRevenueBySubscription() {
        List<Object[]> results = transactionRepository.getRevenueBySubscription();
        List<Map<String, Object>> revenueData = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("subscriptionName", row[0]);
            map.put("revenue", row[1]);
            revenueData.add(map);
        }
        
        return revenueData;
    }
    
    @Override
    public List<Transactions> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    @Override
    public List<Transactions> getActiveTransactions() {
        return transactionRepository.getActiveTransactions();
    }
    
    @Override
    public long getTotalTransactionCount() {
        return transactionRepository.count();
    }
    
    @Override
    public List<Transactions> getRecentTransactions(Integer limit, String statusFilter, String subscriptionFilter) {
        List<Transactions> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        
        // Filter by status if provided
        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equals("ALL")) {
            transactions = transactions.stream()
                    .filter(t -> t.getStatus().name().equals(statusFilter))
                    .toList();
        }
        
        // Filter by subscription if provided
        if (subscriptionFilter != null && !subscriptionFilter.isBlank() && !subscriptionFilter.equals("ALL")) {
            transactions = transactions.stream()
                    .filter(t -> t.getSubscription().getName().equalsIgnoreCase(subscriptionFilter))
                    .toList();
        }
        
        // Limit results
        if (limit != null && limit > 0) {
            transactions = transactions.stream().limit(limit).toList();
        }
        
        return transactions;
    }
    
    @Override
    public Transactions getTransactionById(Integer transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }
    
    // ====================================
    @Override
    public Page<TransactionAdminResponse> getTransactionsPage(Pageable pageable, ComicEnum statusFilter, String usernameFilter) {
        boolean hasStatus = statusFilter != null;
        boolean hasUsername = usernameFilter != null && !usernameFilter.isBlank();

        Page<Transactions> page;
        if (hasStatus && hasUsername) {
            page = transactionRepository.findByStatusAndUserUsernameContaining(statusFilter, usernameFilter, pageable);
        } else if (hasStatus) {
            page = transactionRepository.findByStatusOrderByCreatedAtDesc(statusFilter, pageable);
        } else if (hasUsername) {
            page = transactionRepository.findByUserUsernameContainingOrderByCreatedAtDesc(usernameFilter, pageable);
        } else {
            page = transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return page.map(t -> new TransactionAdminResponse(
                t.getTransactionId(),
                t.getUser().getUsername(),
                t.getSubscription().getName(),
                t.getPrice(),
                t.getStatus(),
                t.getStartedAt(),
                t.getEndedAt(),
                t.getCreatedAt()));
    }

    @Override
    public TransactionSummaryResponse getTransactionSummary() {
        long totalCount = transactionRepository.count();
        BigDecimal totalRevenue = transactionRepository.sumAllPrices();
        return new TransactionSummaryResponse(totalCount, totalRevenue);
    }
}
