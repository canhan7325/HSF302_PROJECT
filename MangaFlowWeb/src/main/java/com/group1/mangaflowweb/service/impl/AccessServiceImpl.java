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

    private static final int DEFAULT_PREVIEW_COUNT = 2;
    private static final int SILVER_PREVIEW_COUNT = 10;

    private final TransactionRepository transactionRepository;

    @Override
    @Deprecated
    public boolean canReadFullChapter(Users user) {
        return getChapterAccess(user).isCanReadFull();
    }

    @Override
    public ChapterAccess getChapterAccess(Users user) {
        if (user == null || user.getUserId() == null) {
            return new ChapterAccess(false, DEFAULT_PREVIEW_COUNT);
        }

        // Find a *valid* subscription: SUCCESS + not expired
        var now = LocalDateTime.now();
        var txOpt = transactionRepository
                .findFirstByUser_UserIdAndStatusAndEndedAtAfterOrderByEndedAtDesc(user.getUserId(), TransactionEnum.SUCCESS, now);

        if (txOpt.isEmpty() || txOpt.get().getSubscription() == null) {
            return new ChapterAccess(false, DEFAULT_PREVIEW_COUNT);
        }

        String subName = txOpt.get().getSubscription().getName();
        subName = (subName == null) ? "" : subName.trim();

        if ("gold".equalsIgnoreCase(subName)) {
            return new ChapterAccess(true, Integer.MAX_VALUE);
        }
        if ("silver".equalsIgnoreCase(subName)) {
            return new ChapterAccess(false, SILVER_PREVIEW_COUNT);
        }

        // Unknown tier: keep current behavior
        return new ChapterAccess(false, DEFAULT_PREVIEW_COUNT);
    }
}
