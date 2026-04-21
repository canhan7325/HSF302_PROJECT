package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.ComicAdRequest;
import com.group1.mangaflowweb.dto.response.admin.ComicAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ComicService {

    // Admin CRUD
    Page<ComicAdminResponse> getComicsPage(Pageable pageable, String search);

    ComicAdminResponse getComicById(Integer id);

    void createComic(ComicAdRequest form);

    void updateComic(Integer id, ComicAdRequest form);

    void softDeleteComic(Integer id);

    List<GenreAdminResponse> getAllGenresWithCount();

    // Legacy / other uses
    long getTotalComics();

    List<Comics> getAllComics();

    List<Comics> searchComicsByName(String name);

    List<Comics> getComicsWithSort(String sortBy, String sortOrder);

    List<Comics> getComicsWithFilter(String sortBy, String sortOrder, String filterBy);
}
