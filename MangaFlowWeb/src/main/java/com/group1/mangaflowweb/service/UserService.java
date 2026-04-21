package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.UserAdRequest;
import com.group1.mangaflowweb.dto.response.admin.UserAdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    long getTotalUsers();

    Page<UserAdminResponse> getUsersPage(Pageable pageable);

    Page<UserAdminResponse> searchUsers(String query, Pageable pageable);

    UserAdminResponse getUserById(Integer id);

    void createUser(UserAdRequest form);

    void updateUser(Integer id, UserAdRequest form);

    void softDeleteUser(Integer id);

    void restoreUser(Integer id);
}
