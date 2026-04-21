package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.GenreAdRequest;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;

import java.util.List;

import com.group1.mangaflowweb.dto.request.GenreRequest;
import com.group1.mangaflowweb.entity.Comics;

public interface GenreService {

    List<GenreAdminResponse> getAllGenresWithCount();

    List<GenreAdminResponse> searchGenres(String name);

    void createGenre(GenreAdRequest form);

    void updateGenre(Integer id, GenreAdRequest form);

    void deleteGenre(Integer id);
    void addGenre(GenreRequest genreRequest, Comics comic);
}
