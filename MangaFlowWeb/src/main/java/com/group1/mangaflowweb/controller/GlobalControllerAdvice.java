package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.TransactionsRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @ModelAttribute("userMembership")
    public String getUserMembership() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();
        Optional<Users> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            return null;
        }

        Users user = userOptional.get();
        logger.info("User: {}, UserId: {}", username, user.getUserId());

        // Query all transactions ordered by createdAt DESC (latest first)
        java.util.List<Transactions> transactions = transactionsRepository.findByUserIdOrderByCreatedAtDesc(
            user.getUserId()
        );

        logger.info("Transactions found: {}", transactions.size());

        if (transactions.isEmpty()) {
            logger.info("No transactions found");
            return null;
        }

        // Get latest transaction (first in list)
        Transactions transaction = transactions.get(0);
        logger.info("Latest Transaction ID: {}, Subscription: {}, Price: {}, CreatedAt: {}",
            transaction.getTransactionId(),
            transaction.getSubscription().getName(),
            transaction.getSubscription().getPrice(),
            transaction.getCreatedAt());

        // Determine membership based on subscription price
        Long price = transaction.getSubscription().getPrice().longValue();
        String membership = null;
        if (price >= 100000) {
            membership = "Hội Viên Vàng";
        } else if (price >= 1000) {
            membership = "Hội Viên Bạc";
        }

        logger.info("Membership determined: {}", membership);
        return membership;
    }
}







