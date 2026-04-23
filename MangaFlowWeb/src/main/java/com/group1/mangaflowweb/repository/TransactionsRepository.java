package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import com.group1.mangaflowweb.enums.TransactionEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {
    boolean existsByUser_UserIdAndStatusAndEndedAtAfter(Integer userId,
                                                        TransactionEnum status,
                                                        LocalDateTime endedAt);

    List<Transactions> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    Optional<Transactions> findFirstByUser_UserIdAndStatusAndEndedAtAfterOrderByEndedAtDesc(
            Integer userId,
            TransactionEnum status,
            LocalDateTime endedAt);

                                                        Page<Transactions> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Transactions> findByStatusAndUserUsernameContaining(TransactionEnum status, String username, Pageable pageable);

    Page<Transactions> findByStatusOrderByCreatedAtDesc(TransactionEnum status, Pageable pageable);

    Page<Transactions> findByUserUsernameContainingOrderByCreatedAtDesc(String username, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Transactions t")
    BigDecimal sumAllPrices();

    // Revenue grouped by ISO week label (e.g. "2024-W03") â€” last 12 weeks, SQL Server native
    @Query(value =
        "SELECT CONCAT(CAST(YEAR(t.created_at) AS VARCHAR), '-W', RIGHT('0' + CAST(DATEPART(iso_week, t.created_at) AS VARCHAR), 2)), " +
        "       SUM(t.price) " +
        "FROM transactions t " +
        "WHERE t.created_at >= :since " +
        "GROUP BY YEAR(t.created_at), DATEPART(iso_week, t.created_at) " +
        "ORDER BY YEAR(t.created_at), DATEPART(iso_week, t.created_at)",
        nativeQuery = true)
    List<Object[]> findRevenueByWeek(@Param("since") LocalDateTime since);

    // Revenue grouped by month label (e.g. "2024-03") â€” last 12 months, SQL Server native
    @Query(value =
        "SELECT CONCAT(CAST(YEAR(t.created_at) AS VARCHAR), '-', RIGHT('0' + CAST(MONTH(t.created_at) AS VARCHAR), 2)), " +
        "       SUM(t.price) " +
        "FROM transactions t " +
        "WHERE t.created_at >= :since " +
        "GROUP BY YEAR(t.created_at), MONTH(t.created_at) " +
        "ORDER BY YEAR(t.created_at), MONTH(t.created_at)",
        nativeQuery = true)
    List<Object[]> findRevenueByMonth(@Param("since") LocalDateTime since);

    // Revenue grouped by year label (e.g. "2024") â€” all years, SQL Server native
    @Query(value =
        "SELECT CAST(YEAR(t.created_at) AS VARCHAR), SUM(t.price) " +
        "FROM transactions t " +
        "GROUP BY YEAR(t.created_at) " +
        "ORDER BY YEAR(t.created_at)",
        nativeQuery = true)
    List<Object[]> findRevenueByYear();

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

    // Get expired transactions that are not yet canceled
    @Query("SELECT t FROM Transactions t WHERE t.endedAt IS NOT NULL AND t.endedAt < CURRENT_TIMESTAMP AND t.statusSubs != 'CANCELED' ORDER BY t.endedAt DESC")
    List<Transactions> getExpiredTransactions();

    // Get all active (non-canceled) transactions by user ID
    @Query("SELECT t FROM Transactions t WHERE t.user.userId = :userId AND t.statusSubs != 'CANCELED' AND (t.endedAt IS NULL OR t.endedAt > CURRENT_TIMESTAMP)")
    List<Transactions> getActiveTransactionsByUserId(@Param("userId") Integer userId);
}

