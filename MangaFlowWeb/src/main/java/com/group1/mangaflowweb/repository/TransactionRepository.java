package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, Integer> {
    
    // Get all transactions by status
    List<Transactions> findByStatus(String status);
    
    // Get transactions by subscription
    List<Transactions> findBySubscriptionSubscriptionId(Integer subscriptionId);
    
    // Get total revenue
    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Transactions t")
    BigDecimal getTotalRevenue();
    
    // Get revenue by subscription
    @Query("SELECT t.subscription.name, COALESCE(SUM(t.price), 0) FROM Transactions t GROUP BY t.subscription.name")
    List<Object[]> getRevenueBySubscription();
    
    // Get active transactions (ongoing)
    @Query("SELECT t FROM Transactions t WHERE t.endedAt IS NULL OR t.endedAt > CURRENT_TIMESTAMP ORDER BY t.createdAt DESC")
    List<Transactions> getActiveTransactions();
}
