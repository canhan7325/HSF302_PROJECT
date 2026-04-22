package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.TransactionsDTO;
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
import java.util.Locale;
import java.text.NumberFormat;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

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
            transaction.setStatus(TransactionEnum.UPDATED);

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
        java.util.List<Transactions> transactions = transactionsRepository.findByUserIdOrderByCreatedAtDesc(userId);

        for (Transactions trans : transactions) {
            // Find the most recent active (SUCCESS status) subscription
            if (trans.getStatus() == TransactionEnum.SUCCESS &&
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
        java.util.List<Transactions> transactions = transactionsRepository.findByUserIdOrderByCreatedAtDesc(userId);

        for (Transactions trans : transactions) {
            // Find silver package transactions that are still active (SUCCESS status)
            if (trans.getStatus() == TransactionEnum.SUCCESS &&
                    trans.getSubscription() != null &&
                    trans.getSubscription().getPrice().longValue() < 100000 &&
                    (trans.getEndedAt() == null || trans.getEndedAt().isAfter(LocalDateTime.now()))) {
                // Cancel this transaction
                trans.setStatus(TransactionEnum.CANCELED);
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
        java.util.List<Transactions> transactions = transactionsRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (transactions.isEmpty()) {
            return 0L;
        }

        Transactions latest = transactions.get(0);
        return latest.getSubscription().getPrice().longValue();
    }

    @Override
    public SubscriptionCheckDTO checkSubscription(Integer userId, Long newSubscriptionPrice,
            BigDecimal newSubscriptionPriceBigDecimal) {
        Long currentPrice = getCurrentMembershipPrice(userId);
        boolean isUpgrade = newSubscriptionPrice > currentPrice;
        boolean isDowngrade = newSubscriptionPrice < currentPrice;
        long discountAmount = 0;

        // Nếu chưa có membership, có thể đăng kí
        if (currentPrice == 0) {
            return SubscriptionCheckDTO.builder()
                    .canSubscribe(true)
                    .currentPrice(0L)
                    .discountAmount(0L)
                    .isUpgrade(false)
                    .build();
        }

        // Nếu đăng kí cùng gói → BLOCK
        if (currentPrice.equals(newSubscriptionPrice)) {
            String membership = getMembershipFromPrice(BigDecimal.valueOf(currentPrice));
            java.util.List<Transactions> transactions = transactionsRepository.findByUserIdOrderByCreatedAtDesc(userId);
            if (!transactions.isEmpty()) {
                Transactions current = transactions.get(0);
                String startDate = current.getStartedAt() != null
                        ? current.getStartedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                String endDate = current.getEndedAt() != null
                        ? current.getEndedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                String message = "Bạn đã là " + membership + " từ ngày " + startDate + " đến ngày " + endDate;
                return SubscriptionCheckDTO.builder()
                        .canSubscribe(false)
                        .message(message)
                        .currentPrice(currentPrice)
                        .discountAmount(0L)
                        .isUpgrade(false)
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
}