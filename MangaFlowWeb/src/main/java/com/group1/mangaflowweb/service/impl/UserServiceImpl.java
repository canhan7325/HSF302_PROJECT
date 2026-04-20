package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }
}
