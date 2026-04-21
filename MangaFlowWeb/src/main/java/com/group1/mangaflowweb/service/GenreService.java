package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.GenreRequest;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;

import java.util.List;

public interface GenreService {

    List<GenreAdminResponse> getAllGenresWithCount();

    List<GenreAdminResponse> searchGenres(String name);

    void createGenre(GenreRequest form);

    void updateGenre(Integer id, GenreRequest form);

    void deleteGenre(Integer id);
}
