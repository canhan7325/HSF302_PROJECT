package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.ChapterAdDTO;
import com.group1.mangaflowweb.dto.response.admin.ChapterAdminResponse;

import java.util.List;

import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.dto.page.PageResponse;

import java.util.List;

public interface ChapterService {
    ChapterResponse create(ChapterDTO request);

    ChapterResponse getById(Integer chapterId);

    List<ChapterResponse> getAll();

    List<ChapterResponse> getByComicId(Integer comicId);

    ChapterResponse update(Integer chapterId, ChapterDTO request);

    void delete(Integer chapterId);

    List<PageResponse> getAllPageByChapterId(Integer chapterId);

    List<ChapterAdminResponse> getChaptersByComic(Integer comicId);

    ChapterAdminResponse getChapterById(Integer chapterId);

    void createChapter(Integer comicId, ChapterAdDTO form);

    void updateChapter(Integer chapterId, ChapterAdDTO form);

    void deleteChapter(Integer chapterId);

}
