package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.UserContextService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionUserContextService implements UserContextService {

    private final HttpSession httpSession;
    private final UserRepository userRepository;

    @Override
    public Optional<Users> getCurrentUser() {
        // Assumption: login flow stores userId in session under key "userId".
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
