package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.GenreRequest;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.stereotype.Service;

@Service
public interface GenreService {
    void addGenre(GenreRequest genreRequest, Comics comic);
}
