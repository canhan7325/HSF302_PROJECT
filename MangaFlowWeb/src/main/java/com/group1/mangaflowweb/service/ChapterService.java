package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterAdminDTO;
import com.group1.mangaflowweb.dto.page.PageDTO;

import java.util.List;

public interface ChapterService {
    ChapterDTO create(ChapterDTO request);

    ChapterDTO getById(Integer chapterId);

    List<ChapterDTO> getAll();

    List<ChapterDTO> getByComicId(Integer comicId);

    ChapterDTO update(Integer chapterId, ChapterDTO request);

    void delete(Integer chapterId);

    List<PageDTO> getAllPageByChapterId(Integer chapterId);

    List<ChapterAdminDTO> getChaptersByComic(Integer comicId);

    ChapterAdminDTO getChapterById(Integer chapterId);

    void createChapter(Integer comicId, ChapterAdminDTO form);

    void updateChapter(Integer chapterId, ChapterAdminDTO form);

    void deleteChapter(Integer chapterId);
}

