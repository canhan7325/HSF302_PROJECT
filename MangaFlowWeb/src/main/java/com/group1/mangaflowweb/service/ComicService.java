package com.group1.mangaflowweb.service;


import com.group1.mangaflowweb.dto.comic.ComicSearchDTO;
import com.group1.mangaflowweb.dto.request.admin.ComicAdDTO;
import com.group1.mangaflowweb.dto.response.admin.ComicAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.group1.mangaflowweb.dto.comic.ComicDTO;
import com.group1.mangaflowweb.dto.comic.ComicResponse;
import java.util.List;

public interface ComicService {

    // Admin CRUD
    Page<ComicAdminResponse> getComicsPage(Pageable pageable, String search);

    ComicAdminResponse getComicById(Integer id);

    void createComic(ComicAdDTO form);

    void updateComic(Integer id, ComicAdDTO form);

    void softDeleteComic(Integer id);

    void hardDeleteComic(Integer id);

    List<GenreAdminResponse> getAllGenresWithCount();

    // Legacy / other uses
    long getTotalComics();

    List<Comics> getAllComics();

    List<Comics> searchComicsByName(String name);

    List<Comics> getComicsWithSort(String sortBy, String sortOrder);

    List<Comics> getComicsWithFilter(String sortBy, String sortOrder, String filterBy);
    ComicResponse create(ComicDTO request);

    ComicResponse getById(Integer comicId);

    ComicResponse getBySlug(String slug);

    List<ComicResponse> getAll();

    List<ComicResponse> getByUserId(Integer userId);

    ComicResponse update(Integer comicId, ComicDTO request);

    void delete(Integer comicId);
    List<ComicSearchDTO> searchByTitle(String query);
    List<ComicResponse> searchForPageByTitle(String query);
}
