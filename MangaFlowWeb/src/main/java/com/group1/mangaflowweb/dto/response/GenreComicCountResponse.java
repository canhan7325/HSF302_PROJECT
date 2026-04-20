package com.group1.mangaflowweb.dto.response;

public record GenreComicCountResponse(
        Integer genreId,
        String genreName,
        long comicCount
) {}
