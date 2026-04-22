package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.service.UserService;
import com.group1.mangaflowweb.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
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
    private TransactionService transactionsService;

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

            // Get user's current membership price
            Long membershipPrice = transactionsService.getCurrentMembershipPrice(user.getUserId());

            // Get membership label from price
            if (membershipPrice > 0) {
                String membership = transactionsService.getMembershipFromPrice(new java.math.BigDecimal(membershipPrice));
                logger.info("User membership: {}", membership);
                return membership;
            }

            return null;
        } catch (Exception e) {
            logger.error("Error getting user membership", e);
            return null;
        }
    }


    @ModelAttribute("hideSearch")
    public boolean shouldHideSearch(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }

        if (path.startsWith("/login") || path.startsWith("/register")
                || path.startsWith("/admin") || path.startsWith("/author")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(role -> "ROLE_ADMIN".equals(role) || "ROLE_AUTHOR".equals(role));
    }
}








