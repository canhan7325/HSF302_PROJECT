package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.GenreAdDTO;
import com.group1.mangaflowweb.dto.response.GenreResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;

import java.util.List;

import com.group1.mangaflowweb.dto.request.GenreDTO;
import com.group1.mangaflowweb.entity.Comics;

public interface GenreService {

    List<GenreAdminResponse> getAllGenresWithCount();

    List<GenreAdminResponse> searchGenres(String name);

    void createGenre(GenreAdDTO form);

    void updateGenre(Integer id, GenreAdDTO form);

    void deleteGenre(Integer id);

    List<GenreResponse> getAllGenres();
}
