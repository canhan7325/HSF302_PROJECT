package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.page.PageRequest;
import com.group1.mangaflowweb.dto.page.PageResponse;

import java.util.List;

public interface PageService {
    PageResponse create(PageRequest request);

    PageResponse getById(Integer pageId);

    List<PageResponse> getAll();

    List<PageResponse> getByChapterId(Integer chapterId);

    PageResponse update(Integer pageId, PageRequest request);

    void delete(Integer pageId);
}
