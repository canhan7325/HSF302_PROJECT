package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.GenreRequest;
import com.group1.mangaflowweb.entity.Comics;

public interface GenreService {
    void addGenre(GenreRequest genreRequest, Comics comic);
}
