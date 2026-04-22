package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.admin.UserAdDTO;
import com.group1.mangaflowweb.dto.response.admin.UserAdminResponse;
import com.group1.mangaflowweb.dto.user.UserResponse;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserAdminResponse> getUsersPage(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(u -> new UserAdminResponse(
                        u.getUserId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRole(),
                        u.getEnabled(),
                        u.getCreatedAt()));
    }

    @Override
    public Page<UserAdminResponse> searchUsers(String query, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable)
                .map(u -> new UserAdminResponse(u.getUserId(), u.getUsername(), u.getEmail(), u.getRole(), u.getEnabled(), u.getCreatedAt()));
    }

    @Override
    public UserAdminResponse getUserById(Integer id) {
        Users u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return new UserAdminResponse(u.getUserId(), u.getUsername(), u.getEmail(), u.getRole(), u.getEnabled(), u.getCreatedAt());
    }

    @Override
    @Transactional
    public void createUser(UserAdDTO form) {
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + form.getUsername());
        }
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + form.getEmail());
        }
        Users user = new Users();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEnabled(true);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists", e);
        }
    }

    @Override
    @Transactional
    public void updateUser(Integer id, UserAdDTO form) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userRepository.findByUsername(form.getUsername())
                .filter(u -> !u.getUserId().equals(id))
                .ifPresent(u -> { throw new IllegalArgumentException("Username already exists: " + form.getUsername()); });

        userRepository.findByEmail(form.getEmail())
                .filter(u -> !u.getUserId().equals(id))
                .ifPresent(u -> { throw new IllegalArgumentException("Email already exists: " + form.getEmail()); });

        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists", e);
        }
    }

    @Override
    @Transactional
    public void softDeleteUser(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }
    @Override
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    @Transactional
    public void registerReader(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("USERNAME_EXISTS");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("EMAIL_EXISTS");
        }

        Users newUser = Users.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role("READER")
                .enabled(true)
                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("USERNAME_OR_EMAIL_EXISTS", e);
        }
    }

    private UserResponse toResponse(Users user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    @Transactional
    public void registerUser(com.group1.mangaflowweb.dto.request.RegisterDTO request) {
        Users newUser = Users.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("READER")
                .enabled(true)
                .build();
        userRepository.save(newUser);
    }
}
