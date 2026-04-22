package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.TransactionsDTO;
import com.group1.mangaflowweb.dto.response.admin.TransactionAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.TransactionSummaryResponse;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.enums.TransactionEnum;
import com.group1.mangaflowweb.repository.SubscriptionRepository;
import com.group1.mangaflowweb.repository.TransactionRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private final SubscriptionRepository subscriptionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  SubscriptionRepository subscriptionRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // ── Payment flow ──────────────────────────────────────────────────────────

    @Override
    public TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Subscriptions subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        Transactions transaction = Transactions.builder()
                .user(user)
                .subscription(subscription)
                .price(price)
                .status(TransactionEnum.PENDING)
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        return toDTO(transactionRepository.save(transaction));
    }

    @Override
    public TransactionsDTO completeTransaction(Integer transactionId) {
        return completeTransaction(transactionId, 0L, false);
    }

    @Override
    public TransactionsDTO completeTransaction(Integer transactionId, Long discountAmount, boolean isUpgrade) {
        Transactions transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        LocalDateTime now = LocalDateTime.now();
        if (transaction.getStartedAt() == null) {
            transaction.setStartedAt(now);
        }
        int durationDays = transaction.getSubscription() != null
                && transaction.getSubscription().getDurationDays() != null
                ? transaction.getSubscription().getDurationDays() : 30;

        LocalDateTime endedAt;
        if (isUpgrade && discountAmount == 60000) {
            transaction.setStatus(TransactionEnum.UPDATED);
            long remaining = getRemainingDaysFromCurrentSubscription(transaction.getUser().getUserId());
            endedAt = now.plusDays(durationDays + remaining);
            cancelPreviousSilverPackage(transaction.getUser().getUserId());
        } else {
            transaction.setStatus(TransactionEnum.SUCCESS);
            endedAt = transaction.getStartedAt().plusDays(durationDays);
        }
        transaction.setEndedAt(endedAt);
        return toDTO(transactionRepository.save(transaction));
    }

    @Override
    public TransactionsDTO createAndCompleteTransaction(Integer userId, Integer subscriptionId, BigDecimal price) {
        TransactionsDTO dto = createTransaction(userId, subscriptionId, price);
        return completeTransaction(dto.getTransactionId());
    }

    // ── Subscription checks ───────────────────────────────────────────────────

    @Override
    public SubscriptionCheckDTO checkSubscription(Integer userId, Long newSubscriptionPrice,
                                                   BigDecimal newSubscriptionPriceBigDecimal) {
        Long currentPrice = getCurrentMembershipPrice(userId);
        boolean isUpgrade = newSubscriptionPrice > currentPrice;
        boolean isDowngrade = newSubscriptionPrice < currentPrice;

        if (currentPrice == 0) {
            return SubscriptionCheckDTO.builder()
                    .canSubscribe(true).currentPrice(0L).discountAmount(0L).isUpgrade(false).build();
        }
        if (currentPrice.equals(newSubscriptionPrice)) {
            String membership = getMembershipFromPrice(BigDecimal.valueOf(currentPrice));
            List<Transactions> txs = transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
            if (!txs.isEmpty()) {
                Transactions current = txs.get(0);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String start = current.getStartedAt() != null ? current.getStartedAt().format(fmt) : "";
                String end   = current.getEndedAt()   != null ? current.getEndedAt().format(fmt)   : "";
                return SubscriptionCheckDTO.builder()
                        .canSubscribe(false)
                        .message("Bạn đã là " + membership + " từ ngày " + start + " đến ngày " + end)
                        .currentPrice(currentPrice).discountAmount(0L).isUpgrade(false).build();
            }
        }
        if (isDowngrade) {
            String currentMembership = getMembershipFromPrice(BigDecimal.valueOf(currentPrice));
            if (newSubscriptionPrice == 0L) {
                return SubscriptionCheckDTO.builder()
                        .canSubscribe(false)
                        .message("Bạn không thể hủy gói " + currentMembership + ". Vui lòng đợi hết hạn gói hiện tại.")
                        .currentPrice(currentPrice).discountAmount(0L).isUpgrade(false).build();
            }
            String newMembership = getMembershipFromPrice(newSubscriptionPriceBigDecimal);
            return SubscriptionCheckDTO.builder()
                    .canSubscribe(false)
                    .message("Bạn không thể hạ từ " + currentMembership + " xuống " + newMembership + ". Vui lòng đợi hết hạn gói hiện tại.")
                    .currentPrice(currentPrice).discountAmount(0L).isUpgrade(false).build();
        }
        long discountAmount = (currentPrice < 100000 && newSubscriptionPrice >= 100000) ? 60000L : 0L;
        return SubscriptionCheckDTO.builder()
                .canSubscribe(true).currentPrice(currentPrice)
                .discountAmount(discountAmount).isUpgrade(isUpgrade).build();
    }

    @Override
    public String getMembershipFromPrice(BigDecimal price) {
        long p = price.longValue();
        if (p >= 100000) return "Hội viên Vàng";
        if (p >= 1000)   return "Hội viên Bạc";
        return null;
    }

    @Override
    public Long getCurrentMembershipPrice(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .filter(t -> t.getStatus() == TransactionEnum.SUCCESS || t.getStatus() == TransactionEnum.UPDATED)
                .filter(t -> t.getEndedAt() == null || t.getEndedAt().isAfter(now))
                .findFirst()
                .map(t -> t.getSubscription().getPrice().longValue())
                .orElse(0L);
    }

    @Override
    public List<Transactions> getTransactionsByUserId(Integer userId) {
        return transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Override
    public BigDecimal getTotalRevenue() {
        return transactionRepository.getTotalRevenue();
    }

    @Override
    public List<Map<String, Object>> getRevenueBySubscription() {
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : transactionRepository.getRevenueBySubscription()) {
            revenueMap.put((String) row[0], (BigDecimal) row[1]);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (com.group1.mangaflowweb.entity.Subscriptions sub : subscriptionRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("subscriptionName", sub.getName());
            map.put("revenue", revenueMap.getOrDefault(sub.getName(), BigDecimal.ZERO));
            result.add(map);
        }
        result.sort((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));
        return result;
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
    public Page<TransactionAdminResponse> getTransactionsPage(Pageable pageable, ComicEnum statusFilter, String usernameFilter) {
        boolean hasStatus   = statusFilter != null;
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
                t.getTransactionId(), t.getUser().getUsername(), t.getSubscription().getName(),
                t.getPrice(), t.getStatus(), t.getStartedAt(), t.getEndedAt(), t.getCreatedAt()));
    }

    @Override
    public TransactionSummaryResponse getTransactionSummary() {
        return new TransactionSummaryResponse(transactionRepository.count(), transactionRepository.sumAllPrices());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void cancelExpiredTransactionsAndDowngradeUsers() {
        List<Transactions> expired = transactionRepository.getExpiredTransactions();
        Set<Users> usersToCheck = new HashSet<>();
        for (Transactions t : expired) {
            t.setStatus(TransactionEnum.CANCELED);
            transactionRepository.save(t);
            usersToCheck.add(t.getUser());
        }
        for (Users user : usersToCheck) {
            if (transactionRepository.getActiveTransactionsByUserId(user.getUserId()).isEmpty()) {
                user.setRole("user");
                userRepository.save(user);
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private long getRemainingDaysFromCurrentSubscription(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .filter(t -> (t.getStatus() == TransactionEnum.SUCCESS || t.getStatus() == TransactionEnum.UPDATED)
                        && (t.getEndedAt() == null || t.getEndedAt().isAfter(now)))
                .findFirst()
                .map(t -> t.getEndedAt() != null ? ChronoUnit.DAYS.between(now, t.getEndedAt()) : 0L)
                .orElse(0L);
    }

    private void cancelPreviousSilverPackage(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .filter(t -> (t.getStatus() == TransactionEnum.SUCCESS || t.getStatus() == TransactionEnum.UPDATED)
                        && t.getSubscription() != null
                        && t.getSubscription().getPrice().longValue() < 100000
                        && (t.getEndedAt() == null || t.getEndedAt().isAfter(now)))
                .findFirst()
                .ifPresent(t -> {
                    t.setStatus(TransactionEnum.CANCELED);
                    transactionRepository.save(t);
                });
    }

    private TransactionsDTO toDTO(Transactions t) {
        return TransactionsDTO.builder()
                .transactionId(t.getTransactionId())
                .price(t.getPrice())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .startedAt(t.getStartedAt())
                .endedAt(t.getEndedAt())
                .createdAt(t.getCreatedAt())
                .userId(t.getUser() != null ? t.getUser().getUserId() : null)
                .subscriptionId(t.getSubscription() != null ? t.getSubscription().getSubscriptionId() : null)
                .subscriptionName(t.getSubscription() != null ? t.getSubscription().getName() : null)
                .build();
    }
}
