package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.response.admin.PageAdminResponse;

import java.util.List;

import com.group1.mangaflowweb.dto.page.PageRequest;
import com.group1.mangaflowweb.dto.page.PageResponse;

import java.util.List;
import java.util.Optional;

public interface PageService {

    List<PageAdminResponse> getPagesByChapter(Integer chapterId);

    String uploadAndAddPage(Integer chapterId, org.springframework.web.multipart.MultipartFile file);

    void addPage(Integer chapterId, String imgPath);

    void deletePage(Integer pageId);

    void reorderPages(Integer chapterId, List<Integer> orderedPageIds);
    PageResponse create(PageRequest request);

    PageResponse getById(Integer pageId);

    List<PageResponse> getAll();

    List<PageResponse> getByChapterId(Integer chapterId);

    Optional<String> getFirstPageImagePath(Integer chapterId);

    PageResponse update(Integer pageId, PageRequest request);

    void delete(Integer pageId);
}
