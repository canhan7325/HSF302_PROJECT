package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.user.UserDTO;
import com.group1.mangaflowweb.dto.user.UserAdminDTO;
import com.group1.mangaflowweb.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    long getTotalUsers();

    Page<UserAdminDTO> getUsersPage(Pageable pageable);

    Page<UserAdminDTO> searchUsers(String query, Pageable pageable);

    UserAdminDTO getUserById(Integer id);

    void createUser(UserAdminDTO form);

    void updateUser(Integer id, UserAdminDTO form);

    void softDeleteUser(Integer id);

    void restoreUser(Integer id);

    UserDTO findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void registerUser(UserDTO request);

    void registerReader(UserDTO registerRequest);

    Users findEntityByUsername(String username);
}

