package com.group1.mangaflowweb.dto.response;

public record DashboardStatsResponse(
        long totalActiveUsers,
        long totalActiveComics,
        long totalViewCount,
        long totalActiveGenres
) {}
