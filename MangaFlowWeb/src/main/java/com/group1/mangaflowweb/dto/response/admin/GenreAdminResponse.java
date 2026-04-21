package com.group1.mangaflowweb.dto.response.admin;

public record GenreAdminResponse(
        Integer genreId,
        String name,
        String slug,
        long comicCount
) {}
