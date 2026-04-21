package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.entity.Users;

import java.util.Optional;

public interface UserContextService {
    Optional<Users> getCurrentUser();
}
