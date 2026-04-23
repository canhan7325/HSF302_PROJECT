package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.user.UserDTO;
import com.group1.mangaflowweb.dto.user.UserAdminDTO;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.UsersRepository;
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

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserAdminDTO> getUsersPage(Pageable pageable) {
        return usersRepository.findAll(pageable)
                .map(u -> UserAdminDTO.builder()
                        .userId(u.getUserId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .enabled(u.getEnabled())
                        .createdAt(u.getCreatedAt())
                        .build());
    }

    @Override
    public Page<UserAdminDTO> searchUsers(String query, Pageable pageable) {
        return usersRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable)
                .map(u -> UserAdminDTO.builder()
                        .userId(u.getUserId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .enabled(u.getEnabled())
                        .createdAt(u.getCreatedAt())
                        .build());
    }

    @Override
    public UserAdminDTO getUserById(Integer id) {
        Users u = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return UserAdminDTO.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole())
                .enabled(u.getEnabled())
                .createdAt(u.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void createUser(UserAdminDTO form) {
        if (usersRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + form.getUsername());
        }
        if (usersRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + form.getEmail());
        }
        Users user = new Users();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEnabled(true);
        try {
            usersRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists", e);
        }
    }

    @Override
    @Transactional
    public void updateUser(Integer id, UserAdminDTO form) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        usersRepository.findByUsername(form.getUsername())
                .filter(u -> !u.getUserId().equals(id))
                .ifPresent(u -> { throw new IllegalArgumentException("Username already exists: " + form.getUsername()); });

        usersRepository.findByEmail(form.getEmail())
                .filter(u -> !u.getUserId().equals(id))
                .ifPresent(u -> { throw new IllegalArgumentException("Email already exists: " + form.getEmail()); });

        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        try {
            usersRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists", e);
        }
    }

    @Override
    @Transactional
    public void softDeleteUser(Integer id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setEnabled(false);
        usersRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Integer id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setEnabled(true);
        usersRepository.save(user);
    }

    @Override
    public long getTotalUsers() {
        return usersRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .map(this::toUserDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    @Transactional
    public void registerReader(UserDTO registerRequest) {
        if (usersRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("USERNAME_EXISTS");
        }
        if (usersRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
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
            usersRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("USERNAME_OR_EMAIL_EXISTS", e);
        }
    }

    private UserDTO toUserDTO(Users user) {
        return UserDTO.builder()
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
        return usersRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usersRepository.findByEmail(email).isPresent();
    }

    @Override
    @Transactional
    public void registerUser(UserDTO request) {
        Users newUser = Users.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("READER")
                .enabled(true)
                .build();
        usersRepository.save(newUser);
    }
}

