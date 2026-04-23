package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.genre.GenreDTO;
import com.group1.mangaflowweb.dto.genre.GenreAdminDTO;

import java.util.List;

public interface GenreService {

    List<GenreAdminDTO> getAllGenresWithCount();

    List<GenreAdminDTO> searchGenres(String name);

    void createGenre(GenreDTO form);

    void updateGenre(Integer id, GenreDTO form);

    void deleteGenre(Integer id);

    List<GenreDTO> getAllGenres();
}

