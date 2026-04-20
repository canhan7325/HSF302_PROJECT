package com.group1.mangaflowweb.dto.response;

public record GenreAdminResponse(
        Integer genreId,
        String name,
        String slug,
        long comicCount
) {}
