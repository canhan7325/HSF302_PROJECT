package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.ChapterAdRequest;
import com.group1.mangaflowweb.dto.response.admin.ChapterAdminResponse;

import java.util.List;

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

    List<ChapterAdminResponse> getChaptersByComic(Integer comicId);

    ChapterAdminResponse getChapterById(Integer chapterId);

    void createChapter(Integer comicId, ChapterAdRequest form);

    void updateChapter(Integer chapterId, ChapterAdRequest form);

    void deleteChapter(Integer chapterId);
}
