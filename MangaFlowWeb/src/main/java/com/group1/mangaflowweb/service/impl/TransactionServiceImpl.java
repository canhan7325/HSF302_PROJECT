package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.repository.TransactionRepository;
import com.group1.mangaflowweb.service.TransactionService;
import org.springframework.stereotype.Service;

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
}
