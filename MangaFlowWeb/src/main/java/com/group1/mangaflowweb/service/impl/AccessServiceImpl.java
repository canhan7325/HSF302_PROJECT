package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.enums.TransactionEnum;
import com.group1.mangaflowweb.repository.TransactionRepository;
import com.group1.mangaflowweb.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean canReadFullChapter(Users user) {
        if (user == null || user.getUserId() == null) return false;

        return transactionRepository.existsByUser_UserIdAndStatusAndEndedAtAfter(
                user.getUserId(),
                TransactionEnum.SUCCESS,
                LocalDateTime.now()
        );
    }
}
