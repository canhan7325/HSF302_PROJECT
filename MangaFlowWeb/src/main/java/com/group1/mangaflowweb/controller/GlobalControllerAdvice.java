package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.service.UserService;
import com.group1.mangaflowweb.service.TransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionsService transactionsService;

    @ModelAttribute("userMembership")
    public String getUserMembership() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();
        
        try {
            var user = userService.findByUsername(username);
            if (user == null) {
                return null;
            }

            logger.info("User: {}, UserId: {}", username, user.getUserId());

            // Get user's latest transaction through service
            // For now, return null if service doesn't have dedicated method
            return null;
        } catch (Exception e) {
            logger.error("Error getting user membership", e);
            return null;
        }
    }
}







