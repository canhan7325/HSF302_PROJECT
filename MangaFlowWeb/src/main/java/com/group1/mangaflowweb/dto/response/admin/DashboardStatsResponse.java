package com.group1.mangaflowweb.dto.response.admin;

public record DashboardStatsResponse(
        long totalActiveUsers,
        long totalActiveComics,
        long totalViewCount,
        long totalActiveGenres
) {}
