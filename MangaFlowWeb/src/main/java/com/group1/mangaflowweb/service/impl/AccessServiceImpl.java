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
    public String getSubscriptionTier(Users user) {
        if (user == null || user.getUserId() == null) {
            return "none";
        }

        // Find a *valid* subscription: SUCCESS or UPDATED + not expired
        var now = LocalDateTime.now();
        var transactions = transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId());

        com.group1.mangaflowweb.entity.Transactions activeTx = null;
        for (var tx : transactions) {
            if ((tx.getStatus() == TransactionEnum.SUCCESS || tx.getStatus() == TransactionEnum.UPDATED) &&
                    (tx.getEndedAt() == null || tx.getEndedAt().isAfter(now))) {
                activeTx = tx;
                break;
            }
        }

        if (activeTx == null || activeTx.getSubscription() == null) {
            return "none";
        }

        String subName = activeTx.getSubscription().getName();
        return (subName == null) ? "none" : subName.trim().toLowerCase();
    }

    @Override
    public ChapterAccess getChapterAccess(Users user) {
        String tier = getSubscriptionTier(user);

        if ("gold".equals(tier)) {
            return new ChapterAccess(true, Integer.MAX_VALUE);
        }
        if ("silver".equals(tier)) {
            return new ChapterAccess(false, SILVER_PREVIEW_COUNT);
        }

        // Unknown tier: keep current behavior
        return new ChapterAccess(false, DEFAULT_PREVIEW_COUNT);
    }
}
