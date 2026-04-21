package com.group1.mangaflowweb.dto.response.admin;

import com.group1.mangaflowweb.enums.ComicEnum;

public record ComicSummaryResponse(
        Integer comicId,
        String title,
        Integer viewCount,
        ComicEnum status
) {}
