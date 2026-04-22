package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.TransactionRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import com.group1.mangaflowweb.dto.response.admin.TransactionAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.TransactionSummaryResponse;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.enums.TransactionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TransactionServiceImpl implements TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
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

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *")  // Run every minute at second 0
    // Cron format: second minute hour day-of-month month day-of-week
    // 0 * * * * * = at second 0 of every minute (every 60 seconds)
    public void cancelExpiredTransactionsAndDowngradeUsers() {
        // Get all expired transactions that are not yet canceled
        List<Transactions> expiredTransactions = transactionRepository.getExpiredTransactions();

        // Set to track which users need to be checked for downgrade
        Set<Users> usersToCheck = new HashSet<>();

        // Cancel all expired transactions
        for (Transactions transaction : expiredTransactions) {
            transaction.setStatus(TransactionEnum.CANCELED);
            transactionRepository.save(transaction);
            usersToCheck.add(transaction.getUser());
        }

        // For each affected user, check if they have any active non-canceled transactions
        for (Users user : usersToCheck) {
            List<Transactions> activeTransactions = transactionRepository.getActiveTransactionsByUserId(user.getUserId());

            // If no active transactions, downgrade user to regular user
            if (activeTransactions.isEmpty()) {
                user.setRole("user");
                userRepository.save(user);
            }
        }
    }
}
