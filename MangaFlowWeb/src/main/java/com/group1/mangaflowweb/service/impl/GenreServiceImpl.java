package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.GenreRequest;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Genres;
import com.group1.mangaflowweb.service.GenreService;
import org.springframework.stereotype.Service;

@Service
public class GenreServiceImpl implements GenreService {
    @Override
    public void addGenre(GenreRequest genreRequest, Comics comic) {
        Genres genre = Genres.builder()
                .name(genreRequest.getName())
                .build();
    }
}
