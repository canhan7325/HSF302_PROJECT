package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.user.UserResponse;

public interface UserService {
    UserResponse findByUsername(String username);
}
