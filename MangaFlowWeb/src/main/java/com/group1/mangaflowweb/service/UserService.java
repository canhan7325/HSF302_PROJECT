package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.UserAdDTO;
import com.group1.mangaflowweb.dto.response.admin.UserAdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.group1.mangaflowweb.dto.user.UserResponse;
import com.group1.mangaflowweb.dto.request.RegisterRequest;

public interface UserService {
    long getTotalUsers();

    Page<UserAdminResponse> getUsersPage(Pageable pageable);

    Page<UserAdminResponse> searchUsers(String query, Pageable pageable);

    UserAdminResponse getUserById(Integer id);

    void createUser(UserAdDTO form);

    void updateUser(Integer id, UserAdDTO form);

    void softDeleteUser(Integer id);

    void restoreUser(Integer id);
    UserResponse findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void registerUser(com.group1.mangaflowweb.dto.request.RegisterDTO request);
}
