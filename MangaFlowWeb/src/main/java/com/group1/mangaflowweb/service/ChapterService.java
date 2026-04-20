package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.chapter.ChapterRequest;
import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.dto.page.PageResponse;

import java.util.List;

public interface ChapterService {
    ChapterResponse create(ChapterRequest request);

    ChapterResponse getById(Integer chapterId);

    List<ChapterResponse> getAll();

    List<ChapterResponse> getByComicId(Integer comicId);

    ChapterResponse update(Integer chapterId, ChapterRequest request);

    void delete(Integer chapterId);

    // requested: get all pages of a chapter
    List<PageResponse> getAllPageByChapterId(Integer chapterId);
}
