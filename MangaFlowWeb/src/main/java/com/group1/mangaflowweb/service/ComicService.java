package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.comic.ComicDTO;
import com.group1.mangaflowweb.dto.comic.ComicAdminDTO;
import com.group1.mangaflowweb.dto.comic.ComicSummaryDTO;
import com.group1.mangaflowweb.dto.genre.GenreAdminDTO;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ComicService {

    // Admin CRUD
    Page<ComicAdminDTO> getComicsPage(Pageable pageable, String search);

    ComicAdminDTO getComicById(Integer id);

    void createComic(ComicAdminDTO form);

    void updateComic(Integer id, ComicAdminDTO form);

    void softDeleteComic(Integer id);

    void hardDeleteComic(Integer id);

    List<GenreAdminDTO> getAllGenresWithCount();

    // Legacy / other uses
    long getTotalComics();

    List<Comics> getAllComics();

    List<Comics> searchComicsByName(String name);

    List<Comics> getComicsWithSort(String sortBy, String sortOrder);

    List<Comics> getComicsWithFilter(String sortBy, String sortOrder, String filterBy);

    ComicDTO create(ComicDTO request);

    ComicDTO getById(Integer comicId);

    ComicDTO getBySlug(String slug);

    List<ComicDTO> getAll();

    List<ComicDTO> getByUserId(Integer userId);

    ComicDTO update(Integer comicId, ComicDTO request);

    void delete(Integer comicId);

    List<ComicSummaryDTO> searchByTitle(String query);

    List<ComicDTO> searchForPageByTitle(String query);
}

