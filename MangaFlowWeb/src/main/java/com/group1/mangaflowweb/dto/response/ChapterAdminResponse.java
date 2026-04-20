package com.group1.mangaflowweb.dto.response;

import java.time.LocalDateTime;

public record ChapterAdminResponse(
        Integer chapterId,
        Integer chapterNumber,
        String title,
        int pageCount,
        LocalDateTime createdAt
) {}
