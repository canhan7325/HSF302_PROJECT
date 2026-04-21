package com.group1.mangaflowweb.dto.response.admin;

import com.group1.mangaflowweb.enums.ComicEnum;

import java.time.LocalDateTime;

public record ComicAdminResponse(
        Integer comicId,
        String title,
        String slug,
        ComicEnum status,
        Integer viewCount,
        int chapterCount,
        String uploaderUsername,
        String coverImg,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
