package com.group1.mangaflowweb.dto.response.admin;

import java.time.LocalDateTime;

public record ChapterAdminResponse(
        Integer chapterId,
        Integer chapterNumber,
        String title,
        int pageCount,
        LocalDateTime createdAt
) {}
