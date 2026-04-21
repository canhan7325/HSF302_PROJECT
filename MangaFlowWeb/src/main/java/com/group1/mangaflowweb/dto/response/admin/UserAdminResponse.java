package com.group1.mangaflowweb.dto.response.admin;

import java.time.LocalDateTime;

public record UserAdminResponse(
        Integer userId,
        String username,
        String email,
        String role,
        Boolean enabled,
        LocalDateTime createdAt
) {}
