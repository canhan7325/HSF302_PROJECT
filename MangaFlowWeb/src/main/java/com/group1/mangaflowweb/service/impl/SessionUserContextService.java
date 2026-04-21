package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.UserContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionUserContextService implements UserContextService {

    private final HttpSession httpSession;
    private final UserRepository userRepository;

    @Override
    public Optional<Users> getCurrentUser() {
        // Prefer Spring Security authentication (JWT stored in session as JWT_TOKEN)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByUsername(authentication.getName());
        }

        // Fallback: older session-based userId (if present)
        Object userIdObj = httpSession.getAttribute("userId");
        if (userIdObj instanceof Integer userId) {
            return userRepository.findById(userId);
        }
        if (userIdObj instanceof String userIdStr) {
            try {
                Integer userId = Integer.valueOf(userIdStr);
                return userRepository.findById(userId);
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
