package com.group1.mangaflowweb.service.impl;

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
        Transactions transaction = transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(TransactionEnum.SUCCESS);
        LocalDateTime now = LocalDateTime.now();

        // Set startedAt only if it's not already set
        if (transaction.getStartedAt() == null) {
            transaction.setStartedAt(now);
        }

        int durationDays = 30;
        if (transaction.getSubscription() != null && transaction.getSubscription().getDurationDays() != null) {
            durationDays = transaction.getSubscription().getDurationDays();
        }
        transaction.setEndedAt(transaction.getStartedAt().plusDays(durationDays));

        Transactions saved = transactionsRepository.save(transaction);
        return toDTO(saved);
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
