package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.subscription.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.transaction.TransactionsDTO;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.enums.TransactionEnum;
import com.group1.mangaflowweb.repository.SubscriptionsRepository;
import com.group1.mangaflowweb.repository.TransactionsRepository;
import com.group1.mangaflowweb.repository.UsersRepository;
import com.group1.mangaflowweb.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.group1.mangaflowweb.dto.transaction.TransactionAdminDTO;
import com.group1.mangaflowweb.dto.transaction.TransactionSummaryDTO;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.text.NumberFormat;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    // --- CLIENTS ---

    private final TransactionsRepository transactionsRepository;
    private final UsersRepository usersRepository;
    private final SubscriptionsRepository subscriptionsRepository;

    @Override
    public TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price) {
        Users user = getUserById(userId);

        Subscriptions subscription = subscriptionsRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        Transactions transaction = Transactions.builder()
                .user(user)
                .subscription(subscription)
                .price(price)
                .status(TransactionEnum.PENDING)
                .statusSubs("ACTIVE")
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Transactions saved = transactionsRepository.save(transaction);
        return toDTO(saved);
    }

    @Override
    public TransactionsDTO completeTransaction(Integer transactionId) {
        return completeTransaction(transactionId, 0L, false);
    }

    @Override
    public TransactionsDTO completeTransaction(Integer transactionId, Long discountAmount, boolean isUpgrade) {
        Transactions transaction = transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        LocalDateTime now = LocalDateTime.now();

        // Set startedAt only if it's not already set
        if (transaction.getStartedAt() == null) {
            transaction.setStartedAt(now);
        }

        // Get duration_days from subscription
        int durationDays = 30;
        if (transaction.getSubscription() != null && transaction.getSubscription().getDurationDays() != null) {
            durationDays = transaction.getSubscription().getDurationDays();
        }

        LocalDateTime endedAt;

        // Nếu upgrade từ Bạc → Vàng: lấy thời gian còn lại của bạc + cộng vào endedAt
        // của vàng
        if (isUpgrade && discountAmount == 60000) {
            transaction.setStatus(TransactionEnum.SUCCESS);

            // Get remaining days from old silver subscription
            long remainingDaysFromOldSubscription = getRemainingDaysFromCurrentSubscription(
                    transaction.getUser().getUserId());

            // endedAt = now + durationDays của vàng + ngày còn lại của bạc
            endedAt = now.plusDays(durationDays + remainingDaysFromOldSubscription);

            // Cancel previous silver package
            cancelPreviousSilverPackage(transaction.getUser().getUserId());
        } else {
            transaction.setStatus(TransactionEnum.SUCCESS);
            // endedAt = startedAt + durationDays từ subscription
            endedAt = transaction.getStartedAt().plusDays(durationDays);
        }

        transaction.setEndedAt(endedAt);

        Transactions saved = transactionsRepository.save(transaction);
        return toDTO(saved);
    }

    /**
     * Get remaining days from current active subscription
     * Trả về số ngày còn lại từ hôm nay đến endedAt của subscription hiện tại
     */
    private long getRemainingDaysFromCurrentSubscription(Integer userId) {
        java.util.List<Transactions> transactions = transactionsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        for (Transactions trans : transactions) {
            // Find the most recent active subscription
            if (trans.getStatus() == TransactionEnum.SUCCESS &&
                    "ACTIVE".equals(trans.getStatusSubs()) &&
                    trans.getSubscription() != null &&
                    (trans.getEndedAt() == null || trans.getEndedAt().isAfter(LocalDateTime.now()))) {

                LocalDateTime now = LocalDateTime.now();
                if (trans.getEndedAt() != null) {
                    return java.time.temporal.ChronoUnit.DAYS.between(now, trans.getEndedAt());
                }
                break;
            }
        }

        return 0L;
    }

    /**
     * Cancel previous silver package when upgrading to gold
     * Gói bạc cũ (SUCCESS) sẽ được đặt thành CANCELED
     */
    private void cancelPreviousSilverPackage(Integer userId) {
        java.util.List<Transactions> transactions = transactionsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        for (Transactions trans : transactions) {
            // Find silver package transactions that are still active
            if (trans.getStatus() == TransactionEnum.SUCCESS &&
                    "ACTIVE".equals(trans.getStatusSubs()) &&
                    trans.getSubscription() != null &&
                    trans.getSubscription().getPrice().longValue() < 100000 &&
                    (trans.getEndedAt() == null || trans.getEndedAt().isAfter(LocalDateTime.now()))) {
                // Cancel this subscription
                trans.setStatusSubs("CANCELED");
                transactionsRepository.save(trans);
                break; // Only cancel the most recent silver package
            }
        }
    }

    @Override
    public TransactionsDTO createAndCompleteTransaction(Integer userId, Integer subscriptionId, BigDecimal price) {
        TransactionsDTO dto = createTransaction(userId, subscriptionId, price);
        return completeTransaction(dto.getTransactionId());
    }

    @Override
    public TransactionsDTO failTransaction(Integer transactionId) {
        Transactions transaction = transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(TransactionEnum.FAILED);
        transaction.setStatusSubs("CANCELED");
        Transactions saved = transactionsRepository.save(transaction);
        return toDTO(saved);
    }

    @Override
    public String getMembershipFromPrice(BigDecimal price) {
        Long priceValue = price.longValue();
        if (priceValue >= 100000) {
            return "Hội viên Vàng";
        } else if (priceValue >= 1000) {
            return "Hội viên Bạc";
        }
        return null;
    }

    @Override
    public Long getCurrentMembershipPrice(Integer userId) {
        // Get all transactions ordered by created date descending
        java.util.List<Transactions> transactions = transactionsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        if (transactions.isEmpty()) {
            return null;
        }

        // Find the first ACTIVE transaction (SUCCESS/UPDATED status and not expired)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (Transactions transaction : transactions) {
            // Check if transaction status is active
            if (transaction.getStatus() != null &&
                transaction.getStatus().equals(TransactionEnum.SUCCESS) &&
                "ACTIVE".equals(transaction.getStatusSubs())) {

                // Check if transaction is not expired
                if (transaction.getEndedAt() == null || transaction.getEndedAt().isAfter(now)) {
                    return transaction.getSubscription().getPrice().longValue();
                }
            }
        }

        return null;
    }

    @Override
    public SubscriptionCheckDTO checkSubscription(Integer userId, Long newSubscriptionPrice,
            BigDecimal newSubscriptionPriceBigDecimal) {
        Long currentPrice = getCurrentMembershipPrice(userId);
        boolean isUpgrade = newSubscriptionPrice > currentPrice;
        boolean isDowngrade = newSubscriptionPrice < currentPrice;
        long discountAmount = 0;

        // Nếu chưa có membership, có thể đăng kí
        if (currentPrice == null) {
            return SubscriptionCheckDTO.builder()
                    .canSubscribe(true)
                    .currentPrice(0L)
                    .discountAmount(0L)
                    .isUpgrade(false)
                    .isCurrent(false)
                    .build();
        }

        // Nếu đăng kí cùng gói → BLOCK
        if (currentPrice.equals(newSubscriptionPrice)) {
            String membership = getMembershipFromPrice(BigDecimal.valueOf(currentPrice));
            if (membership == null && currentPrice == 0) membership = "Gói Miễn Phí";
            
            java.util.List<Transactions> transactions = transactionsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
            if (!transactions.isEmpty()) {
                Transactions current = transactions.get(0);
                String startDate = current.getStartedAt() != null
                        ? current.getStartedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                String endDate = current.getEndedAt() != null
                        ? current.getEndedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                String message = "Bạn đang sử dụng " + membership + (endDate.isEmpty() ? "" : " đến ngày " + endDate);
                return SubscriptionCheckDTO.builder()
                        .canSubscribe(false)
                        .message(message)
                        .currentPrice(currentPrice)
                        .discountAmount(0L)
                        .isUpgrade(false)
                        .isCurrent(true)
                        .build();
            }
        }

        // Downgrade: từ gói cao xuống gói thấp → không cho phép
        // Đặc biệt: Từ Bạc/Vàng xuống FREE (0) → BLOCK
        if (isDowngrade) {
            String currentMembership = getMembershipFromPrice(BigDecimal.valueOf(currentPrice));
            String newMembership = getMembershipFromPrice(newSubscriptionPriceBigDecimal);

            // Nếu downgrade xuống FREE (0) → không cho phép
            if (newSubscriptionPrice == 0L) {
                String message = "Bạn không thể hủy gói " + currentMembership + ". Vui lòng đợi hết hạn gói hiện tại.";
                return SubscriptionCheckDTO.builder()
                        .canSubscribe(false)
                        .message(message)
                        .currentPrice(currentPrice)
                        .discountAmount(0L)
                        .isUpgrade(false)
                        .build();
            }

            // Downgrade giữa các gói có phí (Vàng → Bạc) → cũng không cho phép
            String message = "Bạn không thể hạ từ " + currentMembership + " xuống " + newMembership
                    + ". Vui lòng đợi hết hạn gói hiện tại.";
            return SubscriptionCheckDTO.builder()
                    .canSubscribe(false)
                    .message(message)
                    .currentPrice(currentPrice)
                    .discountAmount(0L)
                    .isUpgrade(false)
                    .build();
        }

        // Upgrade từ Bạc (90k-100k) → Vàng (200k): discount cứng 60k
        if (currentPrice < 100000 && newSubscriptionPrice >= 100000) {
            discountAmount = 60000;
        }

        return SubscriptionCheckDTO.builder()
                .canSubscribe(true)
                .currentPrice(currentPrice)
                .discountAmount(discountAmount)
                .isUpgrade(isUpgrade)
                .isCurrent(false)
                .build();
    }

    /**
     * Convert entity to DTO
     */
    private TransactionsDTO toDTO(Transactions entity) {
        return TransactionsDTO.builder()
                .transactionId(entity.getTransactionId())
                .price(entity.getPrice())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .statusSubs(entity.getStatusSubs())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .subscriptionId(entity.getSubscription() != null ? entity.getSubscription().getSubscriptionId() : null)
                .subscriptionName(entity.getSubscription() != null ? entity.getSubscription().getName() : null)
                .build();
    }

    /**
     * Get user by ID from database
     */
    private Users getUserById(Integer userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public java.util.List<Transactions> getTransactionsByUserId(Integer userId) {
        return transactionsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    // --- ADMIN ---
    @Override
    public BigDecimal getTotalRevenue() {
        return transactionsRepository.getTotalRevenue();
    }
    
    @Override
    public List<Map<String, Object>> getRevenueBySubscription() {
        List<Object[]> results = transactionsRepository.getRevenueBySubscription();
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
        return transactionsRepository.findAll();
    }
    
    @Override
    public List<Transactions> getActiveTransactions() {
        return transactionsRepository.getActiveTransactions();
    }
    
    @Override
    public long getTotalTransactionCount() {
        return transactionsRepository.count();
    }
    // ====================================
    @Override
    public Page<TransactionAdminDTO> getTransactionsPage(Pageable pageable, ComicEnum statusFilter, String usernameFilter) {
        boolean hasStatus = statusFilter != null;
        boolean hasUsername = usernameFilter != null && !usernameFilter.isBlank();

        Page<Transactions> page;
        if (hasStatus && hasUsername) {
            page = transactionsRepository.findByStatusAndUserUsernameContaining(statusFilter, usernameFilter, pageable);
        } else if (hasStatus) {
            page = transactionsRepository.findByStatusOrderByCreatedAtDesc(statusFilter, pageable);
        } else if (hasUsername) {
            page = transactionsRepository.findByUserUsernameContainingOrderByCreatedAtDesc(usernameFilter, pageable);
        } else {
            page = transactionsRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return page.map(t -> TransactionAdminDTO.builder()
                .transactionId(t.getTransactionId())
                .username(t.getUser().getUsername())
                .subscriptionName(t.getSubscription().getName())
                .price(t.getPrice())
                .status(t.getStatus())
                .startedAt(t.getStartedAt())
                .endedAt(t.getEndedAt())
                .createdAt(t.getCreatedAt())
                .build());
    }

    @Override
    public TransactionSummaryDTO getTransactionSummary() {
        long totalCount = transactionsRepository.count();
        BigDecimal totalRevenue = transactionsRepository.sumAllPrices();
        return TransactionSummaryDTO.builder()
                .totalCount(totalCount)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *")  // Run every minute at second 0
    // Cron format: second minute hour day-of-month month day-of-week
    // 0 * * * * * = at second 0 of every minute (every 60 seconds)
    public void cancelExpiredTransactionsAndDowngradeUsers() {
        // Get all expired transactions that are not yet canceled
        List<Transactions> expiredTransactions = transactionsRepository.getExpiredTransactions();

        // Set to track which users need to be checked for downgrade
        Set<Users> usersToCheck = new HashSet<>();

        // Cancel all expired transactions
        for (Transactions transaction : expiredTransactions) {
            transaction.setStatusSubs("CANCELED");
            transactionsRepository.save(transaction);
            usersToCheck.add(transaction.getUser());
        }

        // For each affected user, check if they have any active non-canceled transactions
        for (Users user : usersToCheck) {
            List<Transactions> activeTransactions = transactionsRepository.getActiveTransactionsByUserId(user.getUserId());

            // If no active transactions, downgrade user to regular user
            if (activeTransactions.isEmpty()) {
                user.setRole("user");
                usersRepository.save(user);
            }
        }
    }

}

