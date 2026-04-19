package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.TransactionsDTO;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.SubscriptionsRepository;
import com.group1.mangaflowweb.repository.TransactionsRepository;
import com.group1.mangaflowweb.repository.UsersRepository;
import com.group1.mangaflowweb.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionsRepository;
    private final UsersRepository usersRepository;
    private final SubscriptionsRepository subscriptionsRepository;

    @Override
    public TransactionsDTO createTransaction(Integer userId, Integer subscriptionId, BigDecimal price) {
        Users user = getOrCreateUser(userId);

        Subscriptions subscription = subscriptionsRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        Transactions transaction = Transactions.builder()
                .user(user)
                .subscription(subscription)
                .price(price)
                .status("PENDING")
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Transactions saved = transactionsRepository.save(transaction);
        return toDTO(saved);
    }

    @Override
    public TransactionsDTO completeTransaction(Integer transactionId) {
        Optional<Transactions> optionalTransaction = transactionsRepository.findById(transactionId);
        if (optionalTransaction.isPresent()) {
            Transactions transaction = optionalTransaction.get();
            transaction.setStatus("SUCCESS");
            LocalDateTime now = LocalDateTime.now();
            transaction.setStartedAt(now);

            int durationDays = 30;
            if (transaction.getSubscription() != null && transaction.getSubscription().getDurationDays() != null) {
                durationDays = transaction.getSubscription().getDurationDays();
            }
            transaction.setEndedAt(now.plusDays(durationDays));

            Transactions saved = transactionsRepository.save(transaction);
            return toDTO(saved);
        }
        return null;
    }

    /**
     * Convert entity to DTO
     */
    private TransactionsDTO toDTO(Transactions entity) {
        return TransactionsDTO.builder()
                .transactionId(entity.getTransactionId())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .subscriptionId(entity.getSubscription() != null ? entity.getSubscription().getSubscriptionId() : null)
                .subscriptionName(entity.getSubscription() != null ? entity.getSubscription().getName() : null)
                .build();
    }

    /**
     * Get user by ID, or find any existing user, or create a test user
     */
    private Users getOrCreateUser(Integer userId) {
        return usersRepository.findById(userId).orElseGet(() ->
            usersRepository.findByUsername("testuser").orElseGet(() -> {
                List<Users> allUsers = usersRepository.findAll();
                if (!allUsers.isEmpty()) {
                    return allUsers.get(0);
                }
                Users newUser = Users.builder()
                        .username("testuser")
                        .password("password")
                        .email("test@test.com")
                        .role("user")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                return usersRepository.save(newUser);
            })
        );
    }
}
