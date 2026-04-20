package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.comic.ComicRequest;
import com.group1.mangaflowweb.dto.comic.ComicResponse;

import java.util.List;

public interface ComicService {
    ComicResponse create(ComicRequest request);

    ComicResponse getById(Integer comicId);

    List<ComicResponse> getAll();

    List<ComicResponse> getByUserId(Integer userId);

    ComicResponse update(Integer comicId, ComicRequest request);

    void delete(Integer comicId);
}
