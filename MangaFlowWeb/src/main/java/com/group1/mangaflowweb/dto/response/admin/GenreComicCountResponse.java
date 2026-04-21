package com.group1.mangaflowweb.dto.response.admin;

public record GenreComicCountResponse(
        Integer genreId,
        String genreName,
        long comicCount
) {}
